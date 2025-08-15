package com.diligrp.upay.boot.domain.wechat;

/**
 * 支付ID
 */
public class PaymentId {
    // 支付ID
    private String paymentId;
    // 退款ID
    private String refundId;
    // 模式 - 查询交易状态时使用
    private String mode;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}