package com.diligrp.upay.core.domain;

/**
 * 应用接入许可
 */
public class ApplicationPermit {
    // 应用ID
    private Long appId;
    // 授权Token
    private String token;
    // 商户信息
    private MerchantPermit merchant;

    public Long getAppId() {
        return appId;
    }

    public String getToken() {
        return token;
    }

    public MerchantPermit getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantPermit merchant) {
        this.merchant = merchant;
    }

    public static ApplicationPermit of(Long appId, String token) {
        return of(appId, token, null);
    }

    public static ApplicationPermit of(Long appId, String token, MerchantPermit merchant) {
        ApplicationPermit permit = new ApplicationPermit();
        permit.appId = appId;
        permit.token = token;
        permit.merchant = merchant;

        return permit;
    }
}
