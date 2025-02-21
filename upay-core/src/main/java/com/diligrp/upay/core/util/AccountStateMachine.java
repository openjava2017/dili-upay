package com.diligrp.upay.core.util;

import com.diligrp.upay.core.exception.UserAccountException;
import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.type.AccountState;
import com.diligrp.upay.shared.ErrorCode;

public final class AccountStateMachine {
    /**
     * 校验是否可以冻结资金账户
     */
    public static void AccountVoidStateCheck(UserAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已注销");
        }
    }

    /**
     * 检查是否允许创建子账户
     */
    public static void CheckForRegisterAccount(UserAccount parent) {
        if (parent.getState() == AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "主资金账户已注销");
        }

        if (parent.getParentId() != 0) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "主资金账户才能创建子账户");
        }
    }

    /**
     * 校验是否可以冻结资金账户
     */
    public static void StateCheckForFreeze(UserAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已注销");
        }

        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已被冻结");
        }
    }

    /**
     * 校验是否可以解冻账户
     */
    public static void StateCheckForUnfreeze(UserAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已注销");
        }

        if (account.getState() != AccountState.FROZEN.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户未被冻结");
        }
    }

    /**
     * 校验是否可以注销资金账号
     */
    public static void StateCheckForUnregister(UserAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已注销");
        }
        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new UserAccountException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已冻结");
        }
    }

    /**
     * 校验是否可以注销账号资金
     */
    public static void FundCheckForUnregister(FundAccount fund) {
        if (fund.getBalance() > 0) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "操作失败, 不能注销有余额的资金账户");
        }
        if (fund.getFrozenAmount() != 0) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "操作失败, 不能注销有冻结资金的账户");
        }
        if (fund.getVouchAmount() != 0) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "操作失败, 不能注销有待收款的资金账户");
        }
    }

    /**
     * 根据子账号校验是否可以注销主资金账号
     */
    public static void ChildStateCheckForUnregister(UserAccount child) {
        if (child.getState() != AccountState.VOID.getCode()) {
            throw new UserAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "不能注销该账号：存在子账号");
        }
    }
}
