package com.diligrp.upay.core.domain;

import java.util.function.Consumer;

/**
 * 商户注册申请模型
 */
public class RegisterMerchant {
    // 商户ID
    private Long mchId;
    // 商户名称
    private String name;
    // 父商户ID
    private Long parentId;
    // 商户地址
    private String address;
    // 联系人
    private String linkman;
    // 手机号
    private String telephone;
    // 资金账号密码
    private String password;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        this.linkman = linkman;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void ifParentId(Consumer<Long> consumer) {
        if (parentId != null && parentId != 0) {
            consumer.accept(parentId);
        }
    }

    public Long parentMchId() {
        return parentId != null && parentId != 0 ? parentId : mchId;
    }
}
