package com.diligrp.upay.boot.domain.wechat;

public class OpenId {
    private String openId;

    private String code;

    public static OpenId of(String openId) {
        OpenId data = new OpenId();
        data.openId = openId;
        return data;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
