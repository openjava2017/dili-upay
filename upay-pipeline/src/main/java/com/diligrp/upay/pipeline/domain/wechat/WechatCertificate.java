package com.diligrp.upay.pipeline.domain.wechat;

import java.security.PublicKey;

/**
 * 微信支付平台证书
 */
public class WechatCertificate {
    // 公钥序列号
    private String serialNo;
    // 公钥
    private PublicKey publicKey;

    public static WechatCertificate of(String serialNo, PublicKey publicKey) {
        WechatCertificate certificate = new WechatCertificate();
        certificate.serialNo = serialNo;
        certificate.publicKey = publicKey;
        return certificate;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
