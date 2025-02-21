package com.diligrp.upay.sentinel.domain;

public class PermissionDTO {
    // 权限值
    private Integer permission;
    // 充值风控
    private DepositPermission deposit;
    // 提现风控
    private WithdrawPermission withdraw;
    // 交易风控
    private TradePermission trade;

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
    }

    public DepositPermission getDeposit() {
        return deposit;
    }

    public void setDeposit(DepositPermission deposit) {
        this.deposit = deposit;
    }

    public WithdrawPermission getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(WithdrawPermission withdraw) {
        this.withdraw = withdraw;
    }

    public TradePermission getTrade() {
        return trade;
    }

    public void setTrade(TradePermission trade) {
        this.trade = trade;
    }
}
