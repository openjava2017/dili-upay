package com.diligrp.upay.core.domain;

import java.time.LocalDateTime;

/**
 * 资金事务模型
 */
public class FundTransaction {
    // 用户账户
    private CoreAccount account;
    // 支付ID
    private String paymentId;
    // 业务类型
    private int type;
    // 冻结或解冻金额
    private long frozenAmount;
    // 资金明细
    private FundActivity[] activities;
    // 优惠券配置
    private Configuration configuration;
    // 发生时间
    private LocalDateTime when;

    public CoreAccount getAccount() {
        return account;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public int getType() {
        return type;
    }

    public long getFrozenAmount() {
        return frozenAmount;
    }

    public FundActivity[] getActivities() {
        return activities;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public boolean isFrozenTransaction() {
        return frozenAmount > 0;
    }

    public boolean isUnfrozenTransaction() {
        return frozenAmount < 0;
    }

    public boolean isFundTransaction() {
        return activities != null && activities.length > 0;
    }

    public static FundTransaction of(CoreAccount account, String paymentId, int type, long frozenAmount,
                                     FundActivity[] activities, Configuration configuration, LocalDateTime when) {
        FundTransaction transaction = new FundTransaction();
        transaction.account = account;
        transaction.paymentId = paymentId;
        transaction.type = type;
        transaction.frozenAmount = frozenAmount;
        transaction.activities = activities;
        transaction.configuration = configuration;
        transaction.when = when;
        return transaction;
    }
}
