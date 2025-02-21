package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.exception.RiskControlException;
import com.diligrp.upay.shared.ErrorCode;

/**
 * 交易哨兵领域模型
 */
public class TradeSentinel extends Sentinel {

    private final TradePermission permission;

    public TradeSentinel(TradePermission permission) {
        this.permission = permission;
    }

    @Override
    void checkPassport(Passport passport) {
        if (!Permission.hasPermission(passport.getPermission(), Permission.FOR_TRADE_BUY)) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：买方账户无交易-买权限");
        }
        Long maxAmount = permission.getMaxAmount(); //单笔交易金额
        Long dailyAmount = permission.getDailyAmount(); // 日累计交易金额
        Integer dailyTimes = permission.getDailyTimes(); // 日累计交易次数
        Long monthlyAmount = permission.getMonthlyAmount(); // 月累计交易金额

        // 交易金额不能超过单笔交易限额
        if (maxAmount != null && passport.getAmount() > maxAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：交易金额超过单笔交易限额");
        }
        // 获取执行上下文: 账户日交易金额，日交易次数和月交易金额
        ExecuteContext context = sentinelAssistant.loadTradeExecuteContext(passport);
        if(dailyAmount != null && context.getDailyAmount() + passport.getAmount() > dailyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计交易金额超过日交易限额");
        }
        if (dailyTimes != null && context.getDailyTimes() + 1 > dailyTimes) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计交易次数超过日交易次数");
        }
        if(monthlyAmount != null && context.getMonthlyAmount() + passport.getAmount() > monthlyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计交易金额超过月交易限额");
        }
    }

    @Override
    void admitPassport(Passport passport) {
        sentinelAssistant.refreshTradeExecuteContext(passport);
    }
}
