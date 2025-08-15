package com.diligrp.upay.trade.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信退款结果
 */
public class WechatRefundResult {
    // 微信退款单号
    private String refundId;
    // 原支付ID
    private String paymentId;
    // 支付状态
    private Integer state;
    // 发生时间
    private LocalDateTime when;
    // 交易描述
    private String message;

    public WechatRefundResult(String refundId, String paymentId, int state, LocalDateTime when, String message) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.state = state;
        this.when = when;
        this.message = message;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Integer getState() {
        return state;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public String getMessage() {
        return message;
    }
}
