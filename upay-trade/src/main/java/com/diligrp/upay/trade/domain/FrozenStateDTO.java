package com.diligrp.upay.trade.domain;

import java.time.LocalDateTime;

public class FrozenStateDTO {
    // 支付ID
    private String paymentId;
    // 状态 - 冻结、解冻
    private Integer state;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

    public static FrozenStateDTO of(String paymentId, Integer state, Integer version, LocalDateTime modifiedTime) {
        FrozenStateDTO updateState = new FrozenStateDTO();
        updateState.setPaymentId(paymentId);
        updateState.setState(state);
        updateState.setVersion(version);
        updateState.setModifiedTime(modifiedTime);
        return updateState;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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
}
