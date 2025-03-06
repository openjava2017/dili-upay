package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

public class WechatRefundQuery extends ContainerSupport {
    // 退款ID
    private String refundId;

    public static WechatRefundQuery of(String refundId) {
        WechatRefundQuery query = new WechatRefundQuery();
        query.refundId = refundId;
        return query;
    }

    public String getRefundId() {
        return refundId;
    }
}
