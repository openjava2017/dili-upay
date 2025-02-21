package com.diligrp.upay.trade.domain;

import com.diligrp.upay.core.domain.TransactionStatus;
import com.diligrp.upay.trade.type.PaymentState;

import java.util.Collections;
import java.util.List;

/**
 * 支付结果模型
 */
public class PaymentResult {
    // 交易成功
    public static final int CODE_SUCCESS = PaymentState.SUCCESS.getCode();
    // 交易失败
    public static final int CODE_FAILED = PaymentState.FAILED.getCode();

    // 交易ID
    private String tradeId;
    // 交易状态
    private Integer state;
    // 账户资金
    private List<TransactionStatus> status;
    // 支付描述
    private String message;

    public static PaymentResult of(String tradeId, PaymentState state, String message, TransactionStatus status) {
        return of(tradeId, state, message, Collections.singletonList(status));
    }

    public static PaymentResult of(String tradeId, PaymentState state, String message, List<TransactionStatus> status) {
        PaymentResult result = new PaymentResult();
        result.tradeId = tradeId;
        result.state = state.getCode();
        result.message = message;
        result.status = status;
        return result;
    }

    public String getTradeId() {
        return tradeId;
    }

    public Integer getState() {
        return state;
    }

    public List<TransactionStatus> getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
