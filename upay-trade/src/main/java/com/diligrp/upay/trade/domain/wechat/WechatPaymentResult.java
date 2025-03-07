package com.diligrp.upay.trade.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信支付结果
 */
public class WechatPaymentResult {
    // 支付ID
    protected String paymentId;
    // 支付状态
    protected Integer state;
    // 微信状态码
    protected String stateCode;
    // 发生时间
    protected LocalDateTime when;

    public WechatPaymentResult(String paymentId, int state, String stateCode, LocalDateTime when) {
        this.paymentId = paymentId;
        this.state = state;
        this.stateCode = stateCode;
        this.when = when;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Integer getState() {
        return state;
    }

    public String getStateCode() {
        return stateCode;
    }

    public LocalDateTime getWhen() {
        return when;
    }
}
