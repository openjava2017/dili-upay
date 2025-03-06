package com.diligrp.upay.pipeline.domain.wechat;

/**
 * 微信支付服务商模式子商户信息领域模型
 */
public class WechatMerchant {
    // 商户号
    private String mchId;
    // 小程序ID
    private String appId;

    public static WechatMerchant of(String mchId, String appId) {
        WechatMerchant merchant = new WechatMerchant();
        merchant.mchId = mchId;
        merchant.appId = appId;
        return merchant;
    }

    public String getMchId() {
        return mchId;
    }

    public String getAppId() {
        return appId;
    }
}
