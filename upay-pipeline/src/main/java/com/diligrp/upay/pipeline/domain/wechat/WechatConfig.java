package com.diligrp.upay.pipeline.domain.wechat;

import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付直连模式配置
 */
public class WechatConfig {
    // 微信商户号
    private String mchId;
    // 微信AppId
    private String appId;
    // appSecret，用于小程序支付
    private String appSecret;
    // 商户公钥序列号
    private String serialNo;
    // 商户私钥
    private PrivateKey privateKey;
    // 商户ApiV3Key - Base64编码
    private SecretKeySpec apiV3Key;

    // 微信支付平台证书 - 当旧证书即将过期时，新老证书将并行使用
    // https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/wechatpay5_1.shtml
    // https://pay.weixin.qq.com/docs/merchant/apis/platform-certificate/api-v3-get-certificates/get.html
    private final Map<String, WechatCertificate> certificates = new ConcurrentHashMap<>();

    /**
     * 微信支付通道配置
     *
     * @param mchId - 微信商户号
     * @param appId - 微信小程序ID
     * @param appSecret - 小程序密钥
     * @param serialNo - 商户公钥号
     * @param privateKey - 商户私钥
     * @param apiV3Key - 微信apiV3Key
     */
    public WechatConfig(String mchId, String appId, String appSecret, String serialNo, PrivateKey privateKey, SecretKeySpec apiV3Key) {
        this.mchId = mchId;
        this.appId = appId;
        this.appSecret = appSecret;
        this.serialNo = serialNo;
        this.privateKey = privateKey;
        this.apiV3Key = apiV3Key;
    }

    public String getMchId() {
        return mchId;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public SecretKeySpec getApiV3Key() {
        return apiV3Key;
    }

    public void putCertificate(WechatCertificate certificate) {
        if (certificate != null) {
            certificates.put(certificate.getSerialNo(), certificate);
        }
    }

    public Optional<WechatCertificate> getCertificate(String serialNo) {
        return Optional.ofNullable(certificates.get(serialNo));
    }
}
