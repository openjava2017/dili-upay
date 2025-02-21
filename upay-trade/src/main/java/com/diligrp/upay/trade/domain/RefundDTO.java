package com.diligrp.upay.trade.domain;

import java.util.List;
import java.util.Optional;

/**
 * 交易退款申请，包括：交易撤销、交易退款
 */
public class RefundDTO {
    // 原交易ID
    private String tradeId;
    // 处理金额
    private Long amount;
    // 费用列表
    private List<Fee> fees;
    // 对账周期编号
    private String cycleNo;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
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
