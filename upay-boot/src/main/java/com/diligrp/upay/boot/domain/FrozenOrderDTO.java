package com.diligrp.upay.boot.domain;

import java.time.LocalDateTime;

/**
 * 冻结订单模型
 */
public class FrozenOrderDTO {
    // 支付ID
    private String paymentId;
    // 资金账号
    private Long accountId;
    // 冻结金额
    private Long amount;
    // 冻结状态
    private Integer state;
    // 扩展信息
    private String extension;
    // 冻结时间
    private LocalDateTime freezeTime;
    // 解冻时间
    private LocalDateTime unfreezeTime;
    // 备注
    private String description;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public LocalDateTime getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(LocalDateTime freezeTime) {
        this.freezeTime = freezeTime;
    }

    public LocalDateTime getUnfreezeTime() {
        return unfreezeTime;
    }

    public void setUnfreezeTime(LocalDateTime unfreezeTime) {
        this.unfreezeTime = unfreezeTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static FrozenOrderDTO of(String paymentId, Long accountId, Long amount, Integer state,
        String extension, LocalDateTime freezeTime, LocalDateTime unfreezeTime, String description) {
        FrozenOrderDTO frozenOrder = new FrozenOrderDTO();
        frozenOrder.setPaymentId(paymentId);
        frozenOrder.setAccountId(accountId);
        frozenOrder.setAmount(amount);
        frozenOrder.setState(state);
        frozenOrder.setExtension(extension);
        frozenOrder.setFreezeTime(freezeTime);
        frozenOrder.setUnfreezeTime(unfreezeTime);
        frozenOrder.setDescription(description);
        return frozenOrder;
    }
}
