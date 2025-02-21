package com.diligrp.upay.trade.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

/**
 * 交易撤销、交易退款请求模型
 */
public class Refund extends ContainerSupport {
    // 原交易号
    private String tradeId;
    // 操作金额
    private Long amount;
    // 对账周期编号
    private String cycleNo;

    public static Refund of(String tradeId, Long amount, String cycleNo) {
        Refund refund = new Refund();
        refund.tradeId = tradeId;
        refund.amount = amount;
        refund.cycleNo = cycleNo;
        return refund;
    }

    public String getTradeId() {
        return tradeId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCycleNo() {
        return cycleNo;
    }
}
