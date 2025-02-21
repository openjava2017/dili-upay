package com.diligrp.upay.core.service.impl;

import com.diligrp.upay.core.dao.IFundAccountDao;
import com.diligrp.upay.core.dao.IUserAccountDao;
import com.diligrp.upay.core.domain.AccountStateDto;
import com.diligrp.upay.core.domain.RegisterAccount;
import com.diligrp.upay.core.exception.UserAccountException;
import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.core.type.AccountState;
import com.diligrp.upay.core.util.AccountStateMachine;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.security.PasswordUtils;
import com.diligrp.upay.shared.type.SequenceKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.KeyGeneratorManager;
import com.diligrp.upay.shared.util.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户账号服务实现
 */
@Service("userAccountService")
public class UserAccountServiceImpl implements IUserAccountService {

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createUserAccount(Long mchId, RegisterAccount account) {
        LocalDateTime when = LocalDateTime.now();
        KeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.ACCOUNT_ID.name());
        Long accountId = Long.parseLong(keyGenerator.nextId());
        String secretKey = PasswordUtils.generateSecretKey();
        String password = PasswordUtils.encrypt(account.getPassword(), secretKey);
        long parentId = account.getParentId() == null ? 0L : account.getParentId();

        UserAccount userAccount = UserAccount.builder().customerId(account.getCustomerId()).accountId(accountId)
            .parentId(parentId).type(account.getType()).useFor(account.getUseFor()).name(account.getName())
            .gender(account.getGender()).telephone(account.getTelephone()).email(account.getEmail())
            .idType(account.getIdType()).idCode(account.getIdCode()).address(account.getAddress()).password(password)
            .secretKey(secretKey).state(AccountState.NORMAL.getCode()).mchId(mchId).version(0)
            .createdTime(when).modifiedTime(when).build();
        // 创建子账户检查主资金账户状态
        userAccount.ifChildAccount(act -> {
            Optional<UserAccount> parentOpt = userAccountDao.findByAccountId(account.getParentId());
            parentOpt.orElseThrow(UserAccountException.of(ErrorCode.OPERATION_NOT_ALLOWED, "主资金账户不存在"));
            parentOpt.ifPresent(AccountStateMachine::CheckForRegisterAccount);
        });
        userAccountDao.insertUserAccount(userAccount);

        // 子账户无须创建账户资金，共享主账户资金
        userAccount.ifMasterAccount(act -> {
            FundAccount fundAccount = FundAccount.builder().accountId(accountId).balance(0L).frozenAmount(0L)
                .vouchAmount(0L).mchId(mchId).version(0).createdTime(when).modifiedTime(when).build();
            fundAccountDao.insertFundAccount(fundAccount);
        });

        return accountId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeUserAccount(Long accountId) {
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(accountId);
        UserAccount account = accountOpt.orElseThrow(() -> new UserAccountException(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::StateCheckForFreeze);

        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.FROZEN.getCode(),
            LocalDateTime.now(), account.getVersion());
        Integer result = userAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new UserAccountException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unfreezeUserAccount(Long accountId) {
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(accountId);
        accountOpt.orElseThrow(UserAccountException.of(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::StateCheckForUnfreeze);

        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.NORMAL.getCode(),
            LocalDateTime.now(), accountOpt.get().getVersion());
        Integer result = userAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new UserAccountException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
    }

    /**
     * {@inheritDoc}
     *
     * 注销主账户时所有子账户必须为注销状态，且注销时提供的商户信息须与注册时商户信息一致
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unregisterUserAccount(Long mchId, Long accountId) {
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(accountId);
        UserAccount account = accountOpt.orElseThrow(UserAccountException.of(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
        if (!ObjectUtils.equals(account.getMchId(), mchId)) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "不能注销该商户下的资金账号");
        }
        accountOpt.ifPresent(AccountStateMachine::StateCheckForUnregister);
        Optional<FundAccount> fundOpt = fundAccountDao.findByAccountId(accountId);
        fundOpt.ifPresent(AccountStateMachine::FundCheckForUnregister);

        // 不能注销存在子账号的资金账号
        account.ifMasterAccount(act -> {
            List<UserAccount> children = userAccountDao.listByParentId(account.getAccountId());
            children.forEach(AccountStateMachine::ChildStateCheckForUnregister);
        });
        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.VOID.getCode(),
            LocalDateTime.now(), accountOpt.get().getVersion());
        Integer result = userAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new UserAccountException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount findUserAccountById(Long accountId) {
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(accountId);
        accountOpt.ifPresent(AccountStateMachine::AccountVoidStateCheck);
        return accountOpt.orElseThrow(() -> new UserAccountException(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
    }

    /**
     * {@inheritDoc}
     * 查询指定商户下的用户账户
     */
    @Override
    public UserAccount findUserAccountById(Long mchId, Long accountId) {
        UserAccount userAccount = findUserAccountById(accountId);
        if (!Objects.equals(userAccount.getMchId(), mchId)) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "商户没有权限操作该资金账号");
        }

        return userAccount;
    }
}
