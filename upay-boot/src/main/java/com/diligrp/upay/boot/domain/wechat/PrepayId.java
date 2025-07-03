package com.diligrp.upay.boot.domain.wechat;

/**
 * 预支付ID
 */
public class PrepayId {
    // 支付ID
    private String paymentId;
    // 模式 - 查询交易状态时使用
    private String mode;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}