package com.diligrp.upay.pipeline.domain.wechat;

import java.time.LocalDateTime;

/**
 * 微信支付流水模型
 */
public class WechatStatementDTO {
    // 商户名称
    private String mchName;
    // 交易类型
    private Integer type;
    // 支付ID
    private String paymentId;
    // 支付方式
    private Integer payType;
    // 支付通道ID
    private Long pipelineId;
    // 客户ID
    private Long customerId;
    // 账号名称
    private String name;
    // 微信商品描述
    private String goods;
    // 申请金额-分
    private Long amount;
    // 操作对象-比如：prepareId，二维码，或退款时的原单号
    private String objectId;
    // 付款人
    private String openId;
    // 支付时间
    private LocalDateTime payTime;
    // 微信流水号
    private String outTradeNo;
    // 申请状态
    private Integer state;
    // 备注
    private String description;
    // 交易创建时间
    private LocalDateTime createdTime;

    public String getMchName() {
        return mchName;
    }

    public void setMchName(String mchName) {
        this.mchName = mchName;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
