package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

public class WechatPrepayOrder extends ContainerSupport {
    // 支付ID
    private String paymentId;

    public static WechatPrepayOrder of(String paymentId) {
        WechatPrepayOrder query = new WechatPrepayOrder();
        query.paymentId = paymentId;
        return query;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
