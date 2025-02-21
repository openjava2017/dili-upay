package com.diligrp.upay.sentinel;

/**
 * 常量列表
 */
public final class Constants {
    // 年月
    public static final String YYYYMM = "yyyyMM";
    // 年月日
    public static final String YYYYMMDD = "yyyyMMdd";
    // 一天的秒数
    public static final long ONE_DAY_SECONDS = 3600 * 24;
    // 修改密码时密码错误次数REDIS KEY
    public static final String CHANGE_PASSWORD_KEY = "upay:password:change:%s:%s";
    // 验证密码时密码错误次数REDIS KEY
    public static final String CHECK_PASSWORD_KEY = "upay:password:check:%s:%s";
    // 提现风控日提现金额
    public static final String SENTINEL_WITHDRAW_DAILYAMOUNT = "upay:sentinel:withdraw:%s:dailyAmount:%s";
    // 提现风控日提现次数
    public static final String SENTINEL_WITHDRAW_DAILYTIMES = "upay:sentinel:withdraw:%s:dailyTimes:%s";
    // 提现风控月提现金额
    public static final String SENTINEL_WITHDRAW_MONTHLYAMOUNT = "upay:sentinel:withdraw:%s:monthlyAmount:%s";

    // 交易风控设置前缀
    public static final String SENTINEL_TRADE_PREFIX = "upay:sentinel:trade:";
    // 交易风控日交易金额
    public static final String SENTINEL_TRADE_DAILYAMOUNT = "upay:sentinel:trade:%s:dailyAmount:%s";
    // 交易风控日交易次数
    public static final String SENTINEL_TRADE_DAILYTIMES = "upay:sentinel:trade:%s:dailyTimes:%s";
    // 交易风控月交易金额
    public static final String SENTINEL_TRADE_MONTHLYAMOUNT = "upay:sentinel:trade:%s:monthlyAmount:%s";
}
