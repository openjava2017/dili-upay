package com.diligrp.upay.trade.domain;

/**
 * 免密支付协议查询模型
 */
public class ProtocolQuery {
    // 协议类型
    private Integer type;
    // 账号ID
    private Long accountId;
    // 交易金额
    private Long amount;

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
}
