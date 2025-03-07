package com.diligrp.upay.pipeline.model;

import com.diligrp.upay.shared.model.BaseDO;

public class WechatParam extends BaseDO {
    // 通道ID
    private Long pipelineId;
    // 商户号, 服务商模式下为服务商商户号
    private String mchId;
    // 小程序ID
    private String appId;
    // 小程序密钥
    private String appSecret;
    // 商户公钥序列号
    private String serialNo;
    // 商户私钥
    private String privateKey;
    // 微信公钥序列号
    private String wechatSerialNo;
    // 微信公钥
    private String wechatPublicKey;
    // 微信apiV3Key
    private String apiV3Key;
    // 通道类型: 直连通道或服务商通道
    private Integer type;

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getWechatSerialNo() {
        return wechatSerialNo;
    }

    public void setWechatSerialNo(String wechatSerialNo) {
        this.wechatSerialNo = wechatSerialNo;
    }

    public String getWechatPublicKey() {
        return wechatPublicKey;
    }

    public void setWechatPublicKey(String wechatPublicKey) {
        this.wechatPublicKey = wechatPublicKey;
    }

    public String getApiV3Key() {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
