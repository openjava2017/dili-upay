package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.pipeline.domain.WechatPaymentResponse;
import com.diligrp.upay.pipeline.domain.WechatPrepayRequest;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.impl.WechatPartnerPipeline;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.upay.pipeline.type.WechatPaymentType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.service.IWechatDepositService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("wechatDepositService")
public class WechatDepositServiceImpl implements IWechatDepositService {

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request) {
        WechatPaymentType paymentType = WechatPaymentType.getType(request.getPayType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的微信支付方式"));
        MerchantPermit merchant = application.getMerchant();
        WechatPipeline pipeline = paymentPipelineManager.findPipeline(merchant.getMchId(), WechatPipeline.class);
        if (pipeline instanceof WechatPartnerPipeline) {
            AssertUtils.notEmpty(request.getMchId(), "参数错误: 未提供子商户信息");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator paymentIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = String.valueOf(paymentIdKey.nextId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());

        WechatPrepayRequest prepayRequest = WechatPrepayRequest.of(paymentId, request.getOpenId(), request.getAmount(),
            request.getGoods(), request.getDescription(), now);
        if (paymentType == WechatPaymentType.JSAPI) {
            AssertUtils.notEmpty(request.getOpenId(), "openId missed");
            return pipeline.sendNativePrepayRequest(prepayRequest);
        } else if (paymentType == WechatPaymentType.NATIVE) {
            return pipeline.sendNativePrepayRequest(prepayRequest);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "充值业务不支持此类微信支付方式");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyPaymentResult(WechatPaymentResponse response) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePrepayOrder(WechatPayment payment) {

    }
}
