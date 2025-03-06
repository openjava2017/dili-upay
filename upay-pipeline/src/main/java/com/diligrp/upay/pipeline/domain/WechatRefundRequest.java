package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

import java.time.LocalDateTime;

/**
 * 微信退款领域模型
 */
public class WechatRefundRequest extends ContainerSupport {
    // 退款单号
    private String refundId;
    // 原订单号
    private String paymentId;
    // 原订单总金额
    private Long maxAmount;
    // 退款金额
    private Long amount;
    // 交易备注或退款理由
    private String description;
    // 交易时间
    private LocalDateTime when;

    public static WechatRefundRequest of(String refundId, String paymentId, Long maxAmount, Long amount,
                                         String description, LocalDateTime when) {
        WechatRefundRequest request = new WechatRefundRequest();
        request.refundId = refundId;
        request.paymentId = paymentId;
        request.maxAmount = maxAmount;
        request.amount = amount;
        request.description = description;
        request.when = when;
        return request;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getWhen() {
        return when;
    }
}
