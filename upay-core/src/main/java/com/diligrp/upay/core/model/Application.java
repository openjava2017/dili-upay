package com.diligrp.upay.core.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

/**
 * 接入应用数据模型
 */
public class Application extends BaseDO {
    // 应用ID
    private Long appId;
    // 商户ID
    private Long mchId;
    // 应用名称
    private String name;
    // 授权Token
    private String token;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static Builder builder() {
        return new Application().new Builder();
    }

    public class Builder {
        public Builder appId(Long appId) {
            Application.this.appId = appId;
            return this;
        }

        public Builder mchId(Long mchId) {
            Application.this.mchId = mchId;
            return this;
        }

        public Builder name(String name) {
            Application.this.name = name;
            return this;
        }

        public Builder token(String token) {
            Application.this.token = token;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            Application.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            Application.this.modifiedTime = modifiedTime;
            return this;
        }

        public Application build() {
            return Application.this;
        }
    }
}
