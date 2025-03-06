package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

import java.time.LocalDateTime;

/**
 * 微信支付结果领域模型
 */
public class WechatPaymentResponse extends ContainerSupport {
    // 支付ID
    private String paymentId;
    // 微信订单号
    private String outTradeNo;
    // 支付方openId
    private String openId;
    // 支付时间
    private LocalDateTime when;
    // 支付状态
    private String state;
    // 交易备注
    private String message;

    public static WechatPaymentResponse of(String paymentId, String outTradeNo, String openId,
                                           LocalDateTime when, String state, String message) {
        WechatPaymentResponse response = new WechatPaymentResponse();
        response.paymentId = paymentId;
        response.outTradeNo = outTradeNo;
        response.openId = openId;
        response.when = when;
        response.state = state;
        response.message = message;
        return response;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getOpenId() {
        return openId;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public String getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
