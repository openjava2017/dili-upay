package com.diligrp.upay.boot.component;

import com.diligrp.upay.boot.domain.AccountId;
import com.diligrp.upay.boot.domain.MerchantId;
import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.domain.RegisterAccount;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.core.type.AccountType;
import com.diligrp.upay.core.type.IdType;
import com.diligrp.upay.core.type.UseFor;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.type.Gender;
import com.diligrp.upay.shared.util.AssertUtils;
import jakarta.annotation.Resource;

import java.util.Optional;

/**
 * 账号注册服务组件
 */
@CallableComponent(id = "payment.account.service")
public class AccountServiceComponent {

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 注册资金账号
     */
    public AccountId register(ServiceRequest<RegisterAccount> request) {
        RegisterAccount account = request.getData();
        // 进行入参校验
        AssertUtils.notNull(account.getCustomerId(), "customerId missed");
        AssertUtils.notNull(account.getType(), "type missed");
        AccountType.getType(account.getType()).orElseThrow(() -> new IllegalArgumentException("无效的账号类型"));
        AssertUtils.notNull(account.getUseFor(), "useFor missed");
        UseFor.getType(account.getUseFor()).orElseThrow(() -> new IllegalArgumentException("无效的业务用途"));
        AssertUtils.notEmpty(account.getName(), "name missed");
        Optional.ofNullable(account.getGender()).ifPresent(gender -> Gender.getGender(gender)
            .orElseThrow(() -> new IllegalArgumentException("无效的性别")));
        AssertUtils.notEmpty(account.getTelephone(), "telephone missed");
        Optional.ofNullable(account.getIdType()).ifPresent(idType -> IdType.getType(idType)
            .orElseThrow(() -> new IllegalArgumentException("无效的证件类型")));
        AssertUtils.notEmpty(account.getPassword(), "password missed");
        AssertUtils.isTrue(account.getType() != AccountType.MERCHANT.getCode(), "不能注册商户账号");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        // 资金账号必须归属于主商户，子商户下创建资金账号时自动归属主商户
        long accountId = userAccountService.createUserAccount(permit.getMerchant().parentMchId(), account);
        return AccountId.of(accountId);
    }

    /**
     * 冻结资金账号
     */
    public void freeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        userAccountService.freezeUserAccount(accountId.getAccountId());
    }

    /**
     * 解冻资金账号
     */
    public void unfreeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        userAccountService.unfreezeUserAccount(accountId.getAccountId());
    }

    /**
     * 注销资金账号
     */
    public void unregister(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        userAccountService.unregisterUserAccount(permit.getMerchant().getMchId(), accountId.getAccountId());
    }

    /**
     * 查询商户账户信息
     */
    public MerchantPermit merchant(ServiceRequest<MerchantId> request) {
        MerchantId merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");
        return accessPermitService.loadMerchantPermit(merchant.getMchId());
    }
}
