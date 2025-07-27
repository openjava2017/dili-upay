package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

public class WechatRefundOrder extends ContainerSupport {
    // 退款ID
    private String refundId;

    public static WechatRefundOrder of(String refundId) {
        WechatRefundOrder query = new WechatRefundOrder();
        query.refundId = refundId;
        return query;
    }

    public String getRefundId() {
        return refundId;
    }
}
