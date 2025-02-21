package com.diligrp.upay.boot.domain;

/**
 * 账户余额信息模型
 */
public class FundBalance {
    // 账号ID
    private Long accountId;
    // 账户状态
    private Integer state;
    // 账户余额-分
    private Long balance;
    // 冻结金额-分
    private Long frozenAmount;
    // 担保金额(代收款)-分
    private Long vouchAmount;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(Long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public Long getVouchAmount() {
        return vouchAmount;
    }

    public void setVouchAmount(Long vouchAmount) {
        this.vouchAmount = vouchAmount;
    }

    public static FundBalance of(Long accountId, Long balance, Long frozenAmount, Long vouchAmount) {
        FundBalance fundBalance = new FundBalance();
        fundBalance.setAccountId(accountId);
        fundBalance.setBalance(balance);
        fundBalance.setFrozenAmount(frozenAmount);
        fundBalance.setVouchAmount(vouchAmount);
        return fundBalance;
    }
}
