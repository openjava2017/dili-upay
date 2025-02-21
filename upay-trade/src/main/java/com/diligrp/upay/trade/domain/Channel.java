package com.diligrp.upay.trade.domain;

/**
 * 渠道信息模型 - 银行、微信、支付宝等第三方支付渠道信息
 */
public class Channel {
    /* 银企直连通道请求参数 */
    // 账户编码 - 银企直连的银行卡号，微信二维码信息或小程序临时登录凭证
    private String code;
    // 账户名称 - 银企直连银行卡账户名称
    private String name;
    // 账户类型: 1-个人户，2-企业户
    private Integer type;
    // 业务回调地址
    private String notifyUrl;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
