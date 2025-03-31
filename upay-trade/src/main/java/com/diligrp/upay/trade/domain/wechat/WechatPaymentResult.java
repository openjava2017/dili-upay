package com.diligrp.upay.trade.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信支付结果
 */
public class WechatPaymentResult {
    // 支付ID
    private String paymentId;
    // 支付状态
    private Integer state;
    // 业务系统订单号
    private String outTradeNo;
    // 发生时间
    private LocalDateTime when;
    // 交易描述
    private String message;

    public WechatPaymentResult(String paymentId, int state, String outTradeNo, LocalDateTime when, String message) {
        this.paymentId = paymentId;
        this.state = state;
        this.outTradeNo = outTradeNo;
        this.when = when;
        this.message = message;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Integer getState() {
        return state;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public String getMessage() {
        return message;
    }
}
