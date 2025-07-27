package com.diligrp.upay.pipeline.domain;

/**
 * 微信小程序预支付响应
 */
public class WechatJsApiResponse extends WechatPrepayResponse {
    // 微信预支付ID
    protected String prepayId;
    // 时间戳
    protected String timeStamp;
    // 随机字符串
    protected String nonceStr;
    // 签名类型
    protected String signType;
    // 签名
    protected String paySign;

    public static WechatJsApiResponse of(String paymentId, String prepayId, String timeStamp, String nonceStr,
                                         String signType, String paySign) {
        WechatJsApiResponse response = new WechatJsApiResponse();
        response.paymentId = paymentId;
        response.prepayId = prepayId;
        response.timeStamp = timeStamp;
        response.nonceStr = nonceStr;
        response.signType = signType;
        response.paySign = paySign;

        return response;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public String getPacket() {
        return "prepay_id=" + prepayId;
    }

    public String getSignType() {
        return signType;
    }

    public String getPaySign() {
        return paySign;
    }
}
