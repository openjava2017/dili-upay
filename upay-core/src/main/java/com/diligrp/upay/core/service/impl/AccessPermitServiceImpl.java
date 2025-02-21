package com.diligrp.upay.core.service.impl;

import com.diligrp.upay.core.dao.IMerchantDao;
import com.diligrp.upay.core.domain.*;
import com.diligrp.upay.core.exception.PaymentServiceException;
import com.diligrp.upay.core.model.Application;
import com.diligrp.upay.core.model.Merchant;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.core.type.AccountType;
import com.diligrp.upay.core.type.UseFor;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.RandomUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付平台接入许可服务
 */
@Service("accessPermitService")
public class AccessPermitServiceImpl implements IAccessPermitService {

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IUserAccountService userAccountService;

    private final Map<Long, ApplicationPermit> applications = new ConcurrentHashMap<>();

    private final Map<Long, MerchantPermit> merchants = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * 由于商户一旦创建基本上不会修改，因此可以缓存在本地JVM中；
     * 如后期需要限制商户状态，则只能缓存在REDIS中，商户状态更新时同步更新缓存
     */
    @Override
    public MerchantPermit loadMerchantPermit(Long mchId) {
        MerchantPermit permit = merchants.get(mchId);
        if (permit == null) {
            synchronized (merchants) {
                if ((permit = merchants.get(mchId)) == null) {
                    permit = merchantDao.findByMchId(mchId)
                        .map(mer -> MerchantPermit.of(mer.getMchId(), mer.getName(), mer.getParentId(),
                            mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount()))
                        .orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
                    merchants.put(mchId, permit);
                }
            }
        }
        // Return the copy for safety concern
        return MerchantPermit.of(permit.getMchId(), permit.getName(), permit.getParentId(),
            permit.getProfitAccount(), permit.getVouchAccount(), permit.getPledgeAccount());
    }

    /**
     * {@inheritDoc}
     *
     * 由于应用信息一旦创建基本上不会修改，因此可以缓存在本地JVM中；
     */
    @Override
    public ApplicationPermit loadApplicationPermit(Long appId) {
        ApplicationPermit permit = applications.get(appId);
        if (permit == null) {
            synchronized (applications) {
                if ((permit = applications.get(appId)) == null) {
                    permit = merchantDao.findByAppId(appId)
                        .map(app -> ApplicationPermit.of(app.getAppId(), app.getToken()))
                        .orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "应用信息未注册"));
                    applications.put(appId, permit);
                }
            }
        }
        // Return the copy for safety concern
        return ApplicationPermit.of(permit.getAppId(), permit.getToken());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantPermit registerMerchant(RegisterMerchant request) {
        Optional<Merchant> merchantOpt = merchantDao.findByMchId(request.getMchId());
        merchantOpt.ifPresent(merchant -> { throw new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入商户已存在");});
        request.ifParentId(parentId -> {
            Optional<Merchant> parentOpt = merchantDao.findByMchId(parentId);
            Merchant parent = parentOpt.orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "父商户不存在"));
            if (parent.getParentId() != 0) {
                throw new PaymentServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能在子商户下创建商户");
            }
        });

        LocalDateTime now = LocalDateTime.now();
        // 生成收益账号
        RegisterAccount profileAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PROFIT.getCode()).name(request.getName()).gender(null).telephone(request.getTelephone())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long profileId = userAccountService.createUserAccount(request.parentMchId(), profileAccount);
        // 生成担保账号
        RegisterAccount vouchAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_VOUCH.getCode()).name(request.getName()).gender(null).telephone(request.getTelephone())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long vouchId = userAccountService.createUserAccount(request.parentMchId(), vouchAccount);
        // 生成押金账号
        RegisterAccount pledgeAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PLEDGE.getCode()).name(request.getName()).gender(null).telephone(request.getTelephone())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long pledgeId = userAccountService.createUserAccount(request.parentMchId(), pledgeAccount);

        Merchant merchant = Merchant.builder().mchId(request.getMchId()).state(1)
            .name(request.getName()).parentId(0L).profitAccount(profileId).vouchAccount(vouchId).pledgeAccount(pledgeId)
            .address(request.getAddress()).linkman(request.getLinkman()).telephone(request.getTelephone())
            .createdTime(now).modifiedTime(now).build();
        request.ifParentId(merchant::setParentId);
        merchantDao.insertMerchant(merchant);
        return MerchantPermit.of(request.getMchId(), request.getName(), merchant.getParentId(), profileId, vouchId, pledgeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyMerchant(RegisterMerchant request) {
        Optional<Merchant> merchantOpt = merchantDao.findByMchId(request.getMchId());
        merchantOpt.orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "接入商户不存在") );

        LocalDateTime now = LocalDateTime.now();
        Merchant merchant = Merchant.builder().mchId(request.getMchId()).name(request.getName())
            .address(request.getAddress()).linkman(request.getLinkman()).telephone(request.getTelephone())
            .modifiedTime(now).build();
        merchantDao.updateMerchant(merchant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationPermit registerApplication(RegisterApplication request) {
        Optional<Application> applicationOpt = merchantDao.findByAppId(request.getAppId());
        applicationOpt.ifPresent(app -> { throw new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入应用已存在");});

        LocalDateTime now = LocalDateTime.now();
        ApplicationPermit permit = ApplicationPermit.of(request.getAppId(), RandomUtils.randomString(8));
        Application application = Application.builder().appId(request.getAppId()).mchId(0L)
            .name(request.getName()).token(permit.getToken()).createdTime(now).modifiedTime(now).build();
        merchantDao.insertApplication(application);
        return permit;
    }
}
