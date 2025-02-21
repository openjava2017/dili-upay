package com.diligrp.upay.trade.service.impl;

import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.service.IPreferenceService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.sentinel.service.IUserPasswordService;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.IUserProtocolDao;
import com.diligrp.upay.trade.domain.ProtocolDTO;
import com.diligrp.upay.trade.domain.ProtocolQuery;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.exception.UserProtocolException;
import com.diligrp.upay.trade.model.UserProtocol;
import com.diligrp.upay.trade.service.IPaymentProtocolService;
import com.diligrp.upay.trade.type.ProtocolState;
import com.diligrp.upay.trade.type.ProtocolType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 免密支付协议服务实现
 */
@Service("paymentProtocolService")
public class PaymentProtocolServiceImpl implements IPaymentProtocolService {

    @Resource
    private IUserProtocolDao userProtocolDao;

    @Resource
    private IPreferenceService preferenceService;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IUserPasswordService userPasswordService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProtocol registerUserProtocol(ProtocolDTO request) {
        ProtocolType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的免密协议类型"));
        userProtocolDao.findByAccountId(request.getAccountId(), request.getType())
            .ifPresent(proto -> {throw new TradePaymentException(ErrorCode.OBJECT_ALREADY_EXISTS, "免密协议已存在");});
        UserAccount account = userAccountService.findUserAccountById(request.getAccountId());
        userPasswordService.checkUserPassword(account, request.getPassword(), -1);

        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        Long maxAmount = preferenceService.maxProtocolAmount(String.valueOf(merchant.getMchId()), request.getType());
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PROTOCOL_ID);

        LocalDateTime now = LocalDateTime.now();
        UserProtocol protocol = UserProtocol.builder().protocolId(keyGenerator.nextId()).accountId(request.getAccountId())
            .name(account.getName()).type(request.getType()).minAmount(0L).maxAmount(maxAmount).startOn(now)
            .state(ProtocolState.NORMAL.getCode()).version(0).createdTime(now).build();
        userProtocolDao.insertUserProtocol(protocol);
        return protocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProtocol queryUserProtocol(ProtocolQuery request) {
        ProtocolType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的免密协议类型"));
        UserAccount account = userAccountService.findUserAccountById(request.getAccountId());
        Optional<UserProtocol> protocolOpt = userProtocolDao.findByAccountId(request.getAccountId(), request.getType());
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        Long maxAmount = preferenceService.maxProtocolAmount(String.valueOf(merchant.getMchId()), request.getType());
        protocolOpt.ifPresentOrElse(protocol -> {
            if (protocol.getState() != ProtocolState.NORMAL.getCode()) {
                throw new UserProtocolException(UserProtocolException.USE_NOT_ALLOWED, "不允许使用免密支付，协议已被禁用");
            }
            if (request.getAmount() > maxAmount) {
                throw new UserProtocolException(UserProtocolException.USE_NOT_ALLOWED, "不允许使用免密支付，支付金额超出协议金额范围");
            }
        }, () -> {
            if (request.getAmount() <= maxAmount) {
                throw new UserProtocolException(UserProtocolException.OPEN_ALLOWED, "允许开通免密支付");
            } else {
                throw new UserProtocolException(UserProtocolException.OPEN_NOT_ALLOWED, "不允许开通免密支付，支付金额超出协议金额范围");
            }
        });
        return protocolOpt.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkProtocolPermission(UserAccount account, long protocolId, long amount) {
        Optional<UserProtocol> protoOpt = userProtocolDao.findByProtocolId(protocolId);
        UserProtocol protocol = protoOpt.orElseThrow(() -> new UserProtocolException(ErrorCode.OBJECT_NOT_FOUND, "免密支付协议不存在"));
        // 免密协议中accountId=0时，表示适用于任何资金账号
        if (protocol.getAccountId() > 0 && !ObjectUtils.equals(protocol.getAccountId(), account.getAccountId())) {
            throw new UserProtocolException(ErrorCode.OPERATION_NOT_ALLOWED, "账号未开通免密支付协议");
        }
        if (protocol.getState() != ProtocolState.NORMAL.getCode()) {
            throw new UserProtocolException(ErrorCode.OPERATION_NOT_ALLOWED, "不允许使用免密支付，协议状态异常");
        }
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        long maxAmount = preferenceService.maxProtocolAmount(String.valueOf(merchant.getMchId()), protocol.getType());
        if (amount > maxAmount) {
            throw new UserProtocolException(ErrorCode.OPERATION_NOT_ALLOWED, "不允许使用免密支付，支付金额超出协议金额范围");
        }
    }
}
