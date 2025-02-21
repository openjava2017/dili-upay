package com.diligrp.upay.trade.domain;

import com.diligrp.upay.core.domain.TransactionStatus;

/**
 * 冻结/解冻资金状态领域模型
 */
public class FrozenStatus {
    // 支付ID
    private String paymentId;
    // 账户资金事务状态
    private TransactionStatus status;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public static FrozenStatus of(String paymentId, TransactionStatus status) {
        FrozenStatus frozen = new FrozenStatus();
        frozen.setPaymentId(paymentId);
        frozen.setStatus(status);
        return frozen;
    }
}
