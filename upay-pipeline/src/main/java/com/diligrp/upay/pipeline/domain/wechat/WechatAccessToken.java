package com.diligrp.upay.pipeline.domain.wechat;

/**
 * 微信小程序接口调用凭证
 */
public class WechatAccessToken {
    // 接口调用凭证
    private String token;
    // 凭证有效时间，单位：秒
    private Integer expiresIn;

    public static WechatAccessToken of(String token, Integer expiresIn) {
        WechatAccessToken accessToken = new WechatAccessToken();
        accessToken.token = token;
        accessToken.expiresIn = expiresIn;
        return accessToken;
    }

    public String getToken() {
        return token;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }
}
