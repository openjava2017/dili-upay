package com.diligrp.upay.trade.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信退款结果
 */
public class WechatRefundResult extends WechatPaymentResult {
    // 微信退款单号
    private String refundId;

    public WechatRefundResult(String paymentId, String refundId, int state, String stateCode, LocalDateTime when) {
        super(paymentId, state, stateCode, when);
        this.refundId = refundId;
    }

    public String getRefundId() {
        return refundId;
    }
}
