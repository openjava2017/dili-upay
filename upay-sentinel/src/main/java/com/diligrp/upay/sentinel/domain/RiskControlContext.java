package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.service.ISentinelAssistant;

/**
 * 风险控制上下文
 */
public class RiskControlContext {
    // 充值风控
    private DepositSentinel deposit;
    // 提现风控
    private WithdrawSentinel withdraw;
    // 交易风控
    private TradeSentinel trade;
    // 风控执行助手
    private final ISentinelAssistant sentinelAssistant;

    public RiskControlContext(ISentinelAssistant sentinelAssistant) {
        this.sentinelAssistant = sentinelAssistant;
    }

    public DepositSentinel forDeposit() {
        return this.deposit;
    }

    /**
     * 设置充值风控参数
     */
    public void forDeposit(DepositSentinel deposit) {
        this.deposit = deposit;
        this.deposit.setSentinelAssistant(sentinelAssistant);
    }

    public WithdrawSentinel forWithdraw() {
        return this.withdraw;
    }

    /**
     * 设置提现风控参数
     */
    public void forWithdraw(WithdrawSentinel withdraw) {
        this.withdraw = withdraw;
        this.withdraw.setSentinelAssistant(sentinelAssistant);
    }

    public TradeSentinel forTrade() {
        return this.trade;
    }

    /**
     * 设置交易风控参数
     */
    public void forTrade(TradeSentinel trade) {
        this.trade = trade;
        this.trade.setSentinelAssistant(sentinelAssistant);
    }
}
