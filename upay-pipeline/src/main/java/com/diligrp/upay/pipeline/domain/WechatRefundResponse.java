package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

import java.time.LocalDateTime;

/**
 * 微信退款结果领域模型
 */
public class WechatRefundResponse extends ContainerSupport {
    // 商户退款单号
    private String refundId;
    // 微信退款单号
    private String outTradeNo;
    // 退款完成时间
    private LocalDateTime when;
    // 退款状态
    private String state;
    // 交易备注
    private String message;

    public static WechatRefundResponse of(String refundId, String outTradeNo, LocalDateTime when,
                                          String state, String message) {
        WechatRefundResponse response = new WechatRefundResponse();
        response.refundId = refundId;
        response.outTradeNo = outTradeNo;
        response.when = when;
        response.state = state;
        response.message = message;
        return response;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public String getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
