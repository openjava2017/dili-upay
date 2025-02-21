package com.diligrp.upay.trade.util;

import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.type.AccountState;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.trade.exception.TradePaymentException;

public final class AccountStateMachine {
    public static void checkAccountTradeState(UserAccount account) {
        if (AccountState.VOID.equalTo(account.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, account.getName() + "的资金账户已注销");
        }

        if (AccountState.FROZEN.equalTo(account.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, account.getName() + "的资金账户已冻结");
        }

        if (!AccountState.NORMAL.equalTo(account.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, account.getName() + "的资金账户状态异常");
        }
    }

    /**
     * 校验是否可以解冻或解冻资金, 子账户不允许人工冻结资金
     */
    public static void checkFrozenFund(UserAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已注销");
        }

        if (account.getParentId() != 0) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "子账户不允许人工冻结资金");
        }

        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "资金账户已冻结");
        }
    }
}
