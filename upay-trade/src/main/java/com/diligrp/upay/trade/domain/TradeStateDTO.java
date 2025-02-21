package com.diligrp.upay.trade.domain;

import java.time.LocalDateTime;

/**
 * 交易状态数据传输对象
 */
public class TradeStateDTO {
    // 交易ID
    private String tradeId;
    // 金额 - 预支付交易或发生退款时需要更新实际付款金额
    private Long amount;
    // 预支付交易时需更新实际付款金额
    private Long maxAmount;
    // 费用
    private Long fee;
    // 状态
    private Integer state;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

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

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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

    public static TradeStateDTO of(String tradeId, Integer state, Integer version, LocalDateTime modifiedTime) {
        return TradeStateDTO.of(tradeId, null, state, version, modifiedTime);
    }

    public static TradeStateDTO of(String tradeId, Long amount, Integer state,
                                   Integer version, LocalDateTime modifiedTime) {
        TradeStateDTO tradeState = new TradeStateDTO();
        tradeState.setTradeId(tradeId);
        tradeState.setAmount(amount);
        tradeState.setState(state);
        tradeState.setVersion(version);
        tradeState.setModifiedTime(modifiedTime);
        return tradeState;
    }
}