package com.diligrp.upay.pipeline.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信订单数据处理模型
 */
public class WechatPaymentDTO {
    // 支付ID
    private String paymentId;
    // 微信流水号
    private String outTradeNo;
    // OpenId - 服务商(非子商户)小程序下的openId
    private String openId;
    // 支付时间
    private LocalDateTime payTime;
    // 订单状态
    private Integer state;
    // 交易描述
    private String description;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public static Builder builder() {
        return new WechatPaymentDTO().new Builder();
    }

    public class Builder {
        public Builder paymentId(String paymentId) {
            WechatPaymentDTO.this.paymentId = paymentId;
            return this;
        }

        public Builder outTradeNo(String outTradeNo) {
            WechatPaymentDTO.this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder openId(String openId) {
            WechatPaymentDTO.this.openId = openId;
            return this;
        }

        public Builder payTime(LocalDateTime payTime) {
            WechatPaymentDTO.this.payTime = payTime;
            return this;
        }

        public Builder state(Integer state) {
            WechatPaymentDTO.this.state = state;
            return this;
        }

        public Builder description(String description) {
            WechatPaymentDTO.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            WechatPaymentDTO.this.version = version;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            WechatPaymentDTO.this.modifiedTime = modifiedTime;
            return this;
        }

        public WechatPaymentDTO build() {
            return WechatPaymentDTO.this;
        }
    }
}
