package com.diligrp.upay.trade.domain;

/**
 * 交易冲正申请: 目前只有充值提现会进行冲正操作
 */
public class CorrectDTO {
    // 原交易号
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 冲正金额
    private Long amount;
    // 对账周期编号
    private String cycleNo;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
    }
}
