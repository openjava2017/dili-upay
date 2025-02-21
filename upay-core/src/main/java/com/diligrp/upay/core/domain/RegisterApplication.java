package com.diligrp.upay.core.domain;

/**
 * 应用注册申请模型
 */
public class RegisterApplication {
    // 应用ID
    private Long appId;
    // 应用名称
    private String name;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
