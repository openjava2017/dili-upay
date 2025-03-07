package com.diligrp.upay.trade.domain.wechat;

import com.diligrp.upay.trade.domain.Fee;

import java.util.List;
import java.util.Optional;

/**
 * 微信预支付申请
 */
public class WechatPrepayDTO {
    // 交易类型
    private Integer type;
    // 支付方式
    private Integer payType;
    // 账号ID
    private Long accountId;
    // 商品描述
    private String goods;
    // 申请金额
    private Long amount;
    // 外部流水号
    private String outTradeNo;
    // 费用列表
    private List<Fee> fees;
    // 用户标识
    private String openId;
    // 服务商模式使用 - 子商户号
    private String mchId;
    // 服务商模式使用 - 子商户APPID
    private String appId;
    // 回调地址
    private String notifyUri;
    // 交易描述
    private String description;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public Optional<List<Fee>> fees() {
        return Optional.ofNullable(fees);
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

}
