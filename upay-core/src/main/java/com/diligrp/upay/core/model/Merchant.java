package com.diligrp.upay.core.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

public class Merchant extends BaseDO {
    // 商户ID
    private Long mchId;
    // 商户名称
    private String name;
    // 父商户ID
    private Long parentId;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;
    // 参数配置
    private String param;
    // 商户地址
    private String address;
    // 联系人
    private String linkman;
    // 手机号
    private String telephone;
    // 商户状态
    private Integer state;

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

    public Long getProfitAccount() {
        return profitAccount;
    }

    public void setProfitAccount(Long profitAccount) {
        this.profitAccount = profitAccount;
    }

    public Long getVouchAccount() {
        return vouchAccount;
    }

    public void setVouchAccount(Long vouchAccount) {
        this.vouchAccount = vouchAccount;
    }

    public Long getPledgeAccount() {
        return pledgeAccount;
    }

    public void setPledgeAccount(Long pledgeAccount) {
        this.pledgeAccount = pledgeAccount;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public static Builder builder() {
        return new Merchant().new Builder();
    }

    public class Builder {
        public Builder mchId(Long mchId) {
            Merchant.this.mchId = mchId;
            return this;
        }

        public Builder name(String name) {
            Merchant.this.name = name;
            return this;
        }

        public Builder parentId(Long parentId) {
            Merchant.this.parentId = parentId;
            return this;
        }

        public Builder profitAccount(Long profitAccount) {
            Merchant.this.profitAccount = profitAccount;
            return this;
        }

        public Builder vouchAccount(Long vouchAccount) {
            Merchant.this.vouchAccount = vouchAccount;
            return this;
        }

        public Builder pledgeAccount(Long pledgeAccount) {
            Merchant.this.pledgeAccount = pledgeAccount;
            return this;
        }

        public Builder param(String param) {
            Merchant.this.param = param;
            return this;
        }

        public Builder address(String address) {
            Merchant.this.address = address;
            return this;
        }

        public Builder linkman(String linkman) {
            Merchant.this.linkman = linkman;
            return this;
        }

        public Builder telephone(String telephone) {
            Merchant.this.telephone = telephone;
            return this;
        }

        public Builder state(Integer state) {
            Merchant.this.state = state;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            Merchant.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            Merchant.this.modifiedTime = modifiedTime;
            return this;
        }

        public Merchant build() {
            return Merchant.this;
        }
    }
}
