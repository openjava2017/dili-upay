package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

import java.time.LocalDateTime;

/**
 * 微信预支付申请模型
 */
public class WechatPrepayRequest extends ContainerSupport {
    // 支付ID
    private String paymentId;
    // 用户openId
    private String openId;
    // 交易金额 - 分
    private long amount;
    // 商品描述
    private String goods;
    // 交易备注
    private String description;
    // 交易时间
    private LocalDateTime when;

    public static WechatPrepayRequest of(String paymentId, String openId, long amount, String goods,
                                         String description, LocalDateTime when) {
        WechatPrepayRequest prepay = new WechatPrepayRequest();
        prepay.paymentId = paymentId;
        prepay.openId = openId;
        prepay.amount = amount;
        prepay.goods = goods;
        prepay.description = description;
        prepay.when = when;
        return prepay;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOpenId() {
        return openId;
    }

    public long getAmount() {
        return amount;
    }

    public String getGoods() {
        return goods;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getWhen() {
        return when;
    }
}
