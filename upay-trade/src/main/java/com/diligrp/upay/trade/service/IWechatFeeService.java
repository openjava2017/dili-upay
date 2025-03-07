package com.diligrp.upay.trade.service;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;

/**
 * 微信缴费服务
 */
public interface IWechatFeeService {

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

    /**
     * 微信退款申请
     */
    WechatRefundResult sendRefundRequest(WechatPayment payment, WechatRefundDTO request);

    /**
     * 微信退款完成回调通知
     */
    void notifyRefundResult(WechatRefundResponse response);
}
