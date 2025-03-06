package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

public class WechatPrepayQuery extends ContainerSupport {
    // 支付ID
    private String paymentId;

    public static WechatPrepayQuery of(String paymentId) {
        WechatPrepayQuery query = new WechatPrepayQuery();
        query.paymentId = paymentId;
        return query;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
