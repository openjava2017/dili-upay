package com.diligrp.upay.pipeline.domain;

/**
 * 微信Native预支付响应
 */
public class WechatNativeResponse extends WechatPrepayResponse {
    // 二维码链接
    protected String codeUrl;

    public static WechatNativeResponse of(String paymentId, String codeUrl) {
        WechatNativeResponse response = new WechatNativeResponse();
        response.paymentId = paymentId;
        response.codeUrl = codeUrl;
        return response;
    }

    public String getCodeUrl() {
        return codeUrl;
    }
}
