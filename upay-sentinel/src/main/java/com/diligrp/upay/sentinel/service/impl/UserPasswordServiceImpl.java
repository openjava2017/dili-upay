package com.diligrp.upay.sentinel.service.impl;

import com.diligrp.upay.core.dao.IUserAccountDao;
import com.diligrp.upay.core.domain.AccountStateDto;
import com.diligrp.upay.core.exception.UserAccountException;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.type.AccountState;
import com.diligrp.upay.sentinel.Constants;
import com.diligrp.upay.sentinel.domain.PasswordDTO;
import com.diligrp.upay.sentinel.service.IUserPasswordService;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.redis.LettuceTemplate;
import com.diligrp.upay.shared.security.PasswordUtils;
import com.diligrp.upay.shared.service.ThreadPoolService;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.shared.util.DateUtils;
import com.diligrp.upay.shared.util.ObjectUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 密码管理及验证服务
 */
@Service("userPasswordService")
public class UserPasswordServiceImpl implements IUserPasswordService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPasswordServiceImpl.class);

    private static final int PASSWORD_ERROR_EXPIRE = 60 * 60 * 24 * 2;

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private LettuceTemplate<String, String> lettuceTemplate;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 修改账户密码
     * 密码错误次数不能超过当日最大限制，否则将锁定账户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserPassword(UserAccount account, PasswordDTO request, int maxPwdErrors) {
        // 密码错误次数超过当日限制则不允许修改密码
        // format: upay:password:change:<userId>:20241130
        String userDailyKey = String.format(Constants.CHANGE_PASSWORD_KEY, account.getAccountId(),
            DateUtils.formatDate(LocalDate.now(), Constants.YYYYMMDD));
        if (maxPwdErrors > 0 && currentPasswordErrors(userDailyKey) >= maxPwdErrors) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：密码错误次数超过当日最大限制");
        }
        LocalDateTime when = LocalDateTime.now();
        String password = PasswordUtils.encrypt(request.getPassword(), account.getSecretKey());

        // 原密码校验成功，则修改密码并重置当日密码错误次数
        if (ObjectUtils.equals(password, account.getPassword())) {
            password = PasswordUtils.encrypt(request.getNewPassword(), account.getSecretKey());
            UserAccount userAccount = UserAccount.builder().accountId(account.getAccountId()).password(password)
                .modifiedTime(when).version(account.getVersion()).build();
            int result = userAccountDao.updateUserAccount(userAccount);
            if (result == 0) {
                throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：支付系统忙，请稍后再试");
            }

            // 密码输入正确则重置当日密码错误次数
            if (maxPwdErrors > 0) {
                resetPasswordErrors(userDailyKey);
            }
        } else {
            // 密码输入错误则增加当日密码错误次数，超出最大密码错误次数则不允许修改密码
            if (maxPwdErrors > 0 ) {
                long errors = incPasswordErrors(userDailyKey);
                if (errors >= maxPwdErrors) {
                    throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：密码错误次数超过限制");
                } else if (errors == maxPwdErrors - 2) {
                    throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：原密码错误，还剩2次机会");
                } else if (errors == maxPwdErrors - 1) {
                    throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：原密码错误，还剩1次机会");
                }
            }

            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "修改密码失败：原密码错误");
        }
    }

    /**
     * 验证账户密码
     * 密码错误次数不能超过当日最大限制，否则将锁定账户
     */
    @Override
    public void checkUserPassword(UserAccount account, String password, int maxPwdErrors) {
        AssertUtils.notEmpty(password, "password missed");
        if (AccountState.FROZEN.equalTo(account.getState())) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "用户账户已被冻结");
        }

        LocalDateTime when = LocalDateTime.now();
        String encryptedPwd = PasswordUtils.encrypt(password, account.getSecretKey());
        // format: upay:password:check:<userId>:20241130
        String userDailyKey = String.format(Constants.CHECK_PASSWORD_KEY, account.getAccountId(),
                DateUtils.formatDate(LocalDate.now(), Constants.YYYYMMDD));
        if (!ObjectUtils.equals(encryptedPwd, account.getPassword())) {
            if (maxPwdErrors >= 0) {
                long errors = incPasswordErrors(userDailyKey);
                if (errors >= maxPwdErrors) {
                    // 异步执行，以防止抛出异常后数据库事务回滚导致无法锁定用户账号
                    ThreadPoolService.getIoThreadPoll().submit(() -> {
                        transactionTemplate.execute(status -> { // 线程里使用编程式事务
                            AccountStateDto stateDTO = AccountStateDto.of(account.getAccountId(),
                                    AccountState.FROZEN.getCode(), when, account.getVersion());
                            userAccountDao.compareAndSetState(stateDTO);
                            return null;
                        });
                    });

                    resetPasswordErrors(userDailyKey);
                    throw new UserAccountException(ErrorCode.INVALID_USER_PASSWORD, "用户密码不正确，用户账户已被锁定");
                } else if (errors == maxPwdErrors - 2) {
                    throw new UserAccountException(ErrorCode.INVALID_USER_PASSWORD, "用户密码不正确，再输入错误2次将锁定账号");
                } else if (errors == maxPwdErrors - 1) {
                    throw new UserAccountException(ErrorCode.INVALID_USER_PASSWORD, "用户密码不正确，再输入错误1次将锁定账号");
                }
            }
            throw new UserAccountException(ErrorCode.INVALID_USER_PASSWORD, "用户账户密码不正确");
        }

        // 密码输入正确，重置密码最大错误次数
        if (maxPwdErrors >= 0) {
            resetPasswordErrors(userDailyKey);
        }
    }

    /**
     * 重置用户密码：锁定的用户账号，重置密码后状态变为正常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(UserAccount account, String password) {
        LocalDateTime when = LocalDateTime.now();
        String newPassword = PasswordUtils.encrypt(password, account.getSecretKey());
        // 冻结的账户状态，重置密码后状态变为正常
        Integer state = AccountState.FROZEN.equalTo(account.getState()) ? AccountState.NORMAL.getCode() : null;

        UserAccount userAccount = UserAccount.builder().accountId(account.getAccountId()).password(newPassword)
            .state(state).modifiedTime(when).version(account.getVersion()).build();
        int result = userAccountDao.updateUserAccount(userAccount);

        if (result == 0) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "重置密码失败：支付系统忙，请稍后再试");
        }
    }

    /**
     * Redis缓存获取当前密码错误次数，缓存系统失败返回-1不限制密码错误次数
     */
    private Long currentPasswordErrors(String cachedKey) {
        try {
            String value = lettuceTemplate.get(cachedKey);
            return ObjectUtils.isNotEmpty(value) ? Long.parseLong(value) : 0L;
        } catch (Exception ex) {
            LOG.error("Failed to get current password error times", ex);
            return 0L;
        }
    }

    /**
     * Redis缓存获取某个账号密码错误次数，缓存系统失败则返回-1不限制密码错误次数
     */
    private Long incPasswordErrors(String cachedKey) {
        try {
            return lettuceTemplate.incrAndGet(cachedKey, PASSWORD_ERROR_EXPIRE);
        } catch (Exception ex) {
            LOG.error("Failed to incAndGet password error times", ex);
        }
        return 0L;
    }

    /**
     * Redis缓存重置某个账号密码错误次数
     */
    private void resetPasswordErrors(String cachedKey) {
        try {
            lettuceTemplate.del(cachedKey);
        } catch (Exception ex) {
            LOG.error("Failed to incAndGet password error times", ex);
        }
    }
}
