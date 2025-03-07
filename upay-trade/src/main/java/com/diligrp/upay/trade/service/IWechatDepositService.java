package com.diligrp.upay.trade.service;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.WechatPaymentResponse;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;

/**
 * 微信充值服务
 */
public interface IWechatDepositService {

    /**
     * 微信支付预支付下单
     */
    WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request);

    /**
     * 微信支付完成回调通知
     */
    void notifyPaymentResult(WechatPaymentResponse response);

    /**
     * 关闭微信预支付订单
     */
    void closePrepayOrder(WechatPayment payment);
}
