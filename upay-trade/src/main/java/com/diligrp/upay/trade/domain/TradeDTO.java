package com.diligrp.upay.trade.domain;

/**
 * 交易申请模型
 */
public class TradeDTO {
    // 交易类型
    private Integer type;
    // 账号ID
    private Long accountId;
    // 金额-分
    private Long amount;
    // 外部流水号
    private String outTradeNo;
    // 商品描述
    private String goods;
    // 备注
    private String description;
    // 支付信息
    private PaymentDTO payment;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentDTO getPayment() {
        return payment;
    }

    public void setPayment(PaymentDTO payment) {
        this.payment = payment;
    }
}
