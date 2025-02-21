package com.diligrp.upay.sentinel.domain;

/**
 * 充值风控配置
 */
public class DepositPermission {
    // 单笔限额
    private Long maxAmount;

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void override(DepositPermission permission) {
        if (permission == null) {
            return;
        }
        if (permission.maxAmount != null) {
            this.maxAmount = permission.maxAmount;
        }
    }
}
