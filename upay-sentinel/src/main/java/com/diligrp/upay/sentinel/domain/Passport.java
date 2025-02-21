package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.type.PassportType;

/**
 * 风控通行证领域模型
 */
public class Passport {
    // 通行证类型
    private PassportType type;
    // 账号ID
    private long accountId;
    // 权限值
    private int permission;
    // 发生金额
    private long amount;

    public static Passport of(PassportType type, long accountId, long amount) {
        Passport passport = new Passport();
        passport.type = type;
        passport.accountId = accountId;
        passport.amount = amount;
        return passport;
    }

    public static Passport ofDeposit(long accountId, long amount) {
        return of(PassportType.FOR_DEPOSIT, accountId, amount);
    }

    public static Passport ofWithdraw(long accountId, long amount) {
        return of(PassportType.FOR_WITHDRAW, accountId, amount);
    }

    public static Passport ofTrade(long accountId, long amount) {
        return of(PassportType.FOR_TRADE, accountId, amount);
    }

    public PassportType getType() {
        return type;
    }

    public long getAccountId() {
        return accountId;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public long getAmount() {
        return amount;
    }
}
