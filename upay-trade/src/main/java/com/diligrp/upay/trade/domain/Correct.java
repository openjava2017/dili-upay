package com.diligrp.upay.trade.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

/**
 * 交易冲正请求模型
 */
public class Correct extends ContainerSupport {
    // 原交易号
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 冲正金额
    private Long amount;
    // 对账周期编号
    private String cycleNo;

    public static Correct of(String tradeId, Long accountId, Long amount, String cycleNo) {
        Correct correct = new Correct();
        correct.tradeId = tradeId;
        correct.accountId = accountId;
        correct.amount = amount;
        correct.cycleNo = cycleNo;
        return correct;
    }

    public String getTradeId() {
        return tradeId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCycleNo() {
        return cycleNo;
    }
}
