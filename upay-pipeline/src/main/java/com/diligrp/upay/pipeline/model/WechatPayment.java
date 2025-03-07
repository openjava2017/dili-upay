package com.diligrp.upay.pipeline.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

public class WechatPayment extends BaseDO {
    // 支付商户ID
    private Long mchId;
    // 微信商户号
    private String wxMchId;
    // 微信小程序ID
    private String appId;
    // 订单ID
    private String tradeId;
    // 交易类型
    private Integer type;
    // 支付ID
    private String paymentId;
    // 支付方式
    private Integer payType;
    // 客户ID
    private Long pipelineId;
    // 账号ID
    private Long accountId;
    // 账号名称
    private String name;
    // 微信商品描述
    private String goods;
    // 申请金额-分
    private Long amount;
    // 操作对象-比如：prepareId，二维码，或退款时的原单号
    private String objectId;
    // 主商户小程序下的openId
    private String openId;
    // 支付时间
    private LocalDateTime payTime;
    // 微信流水号
    private String outTradeNo;
    // 申请状态
    private Integer state;
    // 业务回调URI
    private String notifyUri;
    // 备注
    private String description;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getWxMchId() {
        return wxMchId;
    }

    public void setWxMchId(String wxMchId) {
        this.wxMchId = wxMchId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getNotifyUri() {
        return notifyUri;
    }

    public void setNotifyUri(String notifyUri) {
        this.notifyUri = notifyUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Builder builder() {
        return new WechatPayment().new Builder();
    }

    public class Builder {
        public Builder mchId(Long mchId) {
            WechatPayment.this.mchId = mchId;
            return this;
        }

        public Builder wxMchId(String wxMchId) {
            WechatPayment.this.wxMchId = wxMchId;
            return this;
        }

        public Builder appId(String appId) {
            WechatPayment.this.appId = appId;
            return this;
        }

        public Builder tradeId(String tradeId) {
            WechatPayment.this.tradeId = tradeId;
            return this;
        }

        public Builder type(Integer type) {
            WechatPayment.this.type = type;
            return this;
        }

        public Builder paymentId(String paymentId) {
            WechatPayment.this.paymentId = paymentId;
            return this;
        }

        public Builder payType(Integer payType) {
            WechatPayment.this.payType = payType;
            return this;
        }

        public Builder pipelineId(Long pipelineId) {
            WechatPayment.this.pipelineId = pipelineId;
            return this;
        }

        public Builder accountId(Long accountId) {
            WechatPayment.this.accountId = accountId;
            return this;
        }

        public Builder name(String name) {
            WechatPayment.this.name = name;
            return this;
        }

        public Builder goods(String goods) {
            WechatPayment.this.goods = goods;
            return this;
        }

        public Builder amount(Long amount) {
            WechatPayment.this.amount = amount;
            return this;
        }

        public Builder objectId(String objectId) {
            WechatPayment.this.objectId = objectId;
            return this;
        }

        public Builder openId(String openId) {
            WechatPayment.this.openId = openId;
            return this;
        }

        public Builder payTime(LocalDateTime payTime) {
            WechatPayment.this.payTime = payTime;
            return this;
        }

        public Builder outTradeNo(String outTradeNo) {
            WechatPayment.this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder state(Integer state) {
            WechatPayment.this.state = state;
            return this;
        }

        public Builder notifyUri(String notifyUri) {
            WechatPayment.this.notifyUri = notifyUri;
            return this;
        }

        public Builder description(String description) {
            WechatPayment.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            WechatPayment.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            WechatPayment.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            WechatPayment.this.modifiedTime = modifiedTime;
            return this;
        }

        public WechatPayment build() {
            return WechatPayment.this;
        }
    }
}
