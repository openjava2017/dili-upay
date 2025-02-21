package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.exception.RiskControlException;
import com.diligrp.upay.shared.ErrorCode;

/**
 * 提现哨兵领域模型
 */
public class WithdrawSentinel extends Sentinel {
    private final WithdrawPermission permission;

    public WithdrawSentinel(WithdrawPermission permission) {
        this.permission = permission;
    }

    @Override
    void checkPassport(Passport passport) {
        if (!Permission.hasPermission(passport.getPermission(), Permission.FOR_WITHDRAW)) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：用户账号无提现权限");
        }
        Long maxAmount = permission.getMaxAmount(); // 单笔提现金额
        Long dailyAmount = permission.getDailyAmount(); // 日累计提现金额
        Integer dailyTimes = permission.getDailyTimes(); // 日累计提现次数
        Long monthlyAmount = permission.getMonthlyAmount(); // 月累计提现金额

        // 提现金额不能超过单笔提现限额
        if (maxAmount != null && passport.getAmount() > maxAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：提现金额超过单笔提现限额");
        }
        // 获取执行上下文: 账户日提现金额，日提现次数和月提现金额
        ExecuteContext context = sentinelAssistant.loadWithdrawExecuteContext(passport);
        if(dailyAmount != null && context.getDailyAmount() + passport.getAmount() > dailyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现金额超过日提现限额");
        }
        if (dailyTimes != null && context.getDailyTimes() + 1 > dailyTimes) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现次数超过日提现次数");
        }
        if(monthlyAmount != null && context.getMonthlyAmount() + passport.getAmount() > monthlyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现金额超过月提现限额");
        }
    }

    @Override
    void admitPassport(Passport passport) {
        sentinelAssistant.refreshWithdrawExecuteContext(passport);
    }
}
