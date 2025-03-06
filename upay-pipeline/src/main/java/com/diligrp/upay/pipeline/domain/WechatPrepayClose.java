package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

public class WechatPrepayClose extends ContainerSupport {
    // 支付ID
    private String paymentId;

    public static WechatPrepayClose of(String paymentId) {
        WechatPrepayClose query = new WechatPrepayClose();
        query.paymentId = paymentId;
        return query;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
