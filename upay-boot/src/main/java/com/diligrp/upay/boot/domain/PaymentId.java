package com.diligrp.upay.boot.domain;

/**
 * 账号冻结接口层模型
 */
public class PaymentId {
    // 冻结ID
    private String paymentId;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public static PaymentId of(String paymentId) {
        PaymentId payment = new PaymentId();
        payment.setPaymentId(paymentId);
        return payment;
    }
}
