package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.exception.RiskControlException;
import com.diligrp.upay.shared.ErrorCode;

/**
 * 充值哨兵领域模型
 */
public class DepositSentinel extends Sentinel {

    private final DepositPermission permission;

    public DepositSentinel(DepositPermission permission) {
        this.permission = permission;
    }

    @Override
    void checkPassport(Passport passport) {
        if (!Permission.hasPermission(passport.getPermission(), Permission.FOR_DEPOSIT)) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：用户账号无充值权限");
        }
        Long maxAmount = permission.getMaxAmount(); // 单笔充值金额
        // 充值金额不能超过单笔充值限额
        if (maxAmount != null && passport.getAmount() > maxAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：充值金额超过单笔充值限额");
        }
    }

    @Override
    void admitPassport(Passport passport) {
        // Ignore it
    }
}
