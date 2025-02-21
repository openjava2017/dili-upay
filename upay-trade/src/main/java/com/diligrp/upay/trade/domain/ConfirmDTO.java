package com.diligrp.upay.trade.domain;

import java.util.List;
import java.util.Optional;

/**
 * 预授权交易确认消费请求模型
 */
public class ConfirmDTO {
    // 交易ID
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 确认消费金额
    private Long amount;
    // 支付密码
    private String password;
    // 缴费
    private List<Fee> fees;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
    }

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }
}