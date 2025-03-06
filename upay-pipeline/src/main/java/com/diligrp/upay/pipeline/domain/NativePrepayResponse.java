package com.diligrp.upay.pipeline.domain;

/**
 * 微信Native预支付响应
 */
public class NativePrepayResponse extends WechatPrepayResponse {
    // 二维码链接
    protected String codeUrl;

    public static NativePrepayResponse of(String paymentId, String codeUrl) {
        NativePrepayResponse response = new NativePrepayResponse();
        response.paymentId = paymentId;
        response.codeUrl = codeUrl;
        return response;
    }

    public String getCodeUrl() {
        return codeUrl;
    }
}
