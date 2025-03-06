package com.diligrp.upay.pipeline.domain;

/**
 * 微信预支付
 */
public class WechatPrepayResponse {
    // 支付订单号
    protected String paymentId;

    public String getPaymentId() {
        return paymentId;
    }
}
