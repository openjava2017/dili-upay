package com.diligrp.upay.boot.domain.wechat;

/**
 * 退款ID
 */
public class RefundId {
    // 退款ID
    private String refundId;
    // 模式 - 查询交易状态时使用
    private String mode;

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