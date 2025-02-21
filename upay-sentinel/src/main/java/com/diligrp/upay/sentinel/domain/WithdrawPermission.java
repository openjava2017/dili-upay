package com.diligrp.upay.sentinel.domain;

/**
 * 提现风控配置
 */
public class WithdrawPermission {
    // 单笔限额
    private Long maxAmount;
    // 日限额
    private Long dailyAmount;
    // 日次数
    private Integer dailyTimes;
    // 月限额
    private Long monthlyAmount;

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getDailyAmount() {
        return dailyAmount;
    }

    public void setDailyAmount(Long dailyAmount) {
        this.dailyAmount = dailyAmount;
    }

    public Integer getDailyTimes() {
        return dailyTimes;
    }

    public void setDailyTimes(Integer dailyTimes) {
        this.dailyTimes = dailyTimes;
    }

    public Long getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(Long monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public void override(WithdrawPermission permission) {
        if (permission == null) {
            return;
        }
        if (permission.maxAmount != null) {
            this.maxAmount = permission.maxAmount;
        }
        if (permission.dailyAmount != null) {
            this.dailyAmount = permission.dailyAmount;
        }
        if (permission.dailyTimes != null) {
            this.dailyTimes = permission.dailyTimes;
        }
        if (permission.monthlyAmount != null) {
            this.monthlyAmount = permission.monthlyAmount;
        }
    }
}
