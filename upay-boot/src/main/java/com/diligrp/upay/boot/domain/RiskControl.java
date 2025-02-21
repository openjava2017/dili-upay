package com.diligrp.upay.boot.domain;

import com.diligrp.upay.sentinel.domain.DepositPermission;
import com.diligrp.upay.sentinel.domain.TradePermission;
import com.diligrp.upay.sentinel.domain.WithdrawPermission;

import java.util.List;

/**
 * 交易权限及风控信息
 */
public class RiskControl {
    // 商户ID
    private Long mchId;
    // 资金账号
    private Long accountId;
    // 已有交易权限码
    private List<Integer> permissions;
    // 全量权限码
    private List<Option> allPermission;
    // 充值风控
    private DepositPermission deposit;
    // 提现风控
    private WithdrawPermission withdraw;
    // 交易风控
    private TradePermission trade;
    // 交易密码
    private String password;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public List<Integer> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Integer> permissions) {
        this.permissions = permissions;
    }

    public List<Option> getAllPermission() {
        return allPermission;
    }

    public void setAllPermission(List<Option> allPermission) {
        this.allPermission = allPermission;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
