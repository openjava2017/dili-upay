package com.diligrp.upay.pipeline.domain;

/**
 * 微信小程序预支付响应
 */
public class JsApiPrepayResponse extends WechatPrepayResponse {
    // 时间戳
    protected String timeStamp;
    // 随机字符串
    protected String nonceStr;
    // 下单信息
    protected String packet;
    // 签名类型
    protected String signType;
    // 签名
    protected String paySign;

    public static JsApiPrepayResponse of(String paymentId, String timeStamp, String nonceStr, String packet,
                                         String signType, String paySign) {
        JsApiPrepayResponse response = new JsApiPrepayResponse();
        response.paymentId = paymentId;
        response.timeStamp = timeStamp;
        response.nonceStr = nonceStr;
        response.packet = packet;
        response.signType = signType;
        response.paySign = paySign;

        return response;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public String getPacket() {
        return packet;
    }

    public String getSignType() {
        return signType;
    }

    public String getPaySign() {
        return paySign;
    }
}
