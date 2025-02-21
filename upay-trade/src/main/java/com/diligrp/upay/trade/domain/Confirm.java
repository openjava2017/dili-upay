package com.diligrp.upay.trade.domain;

import com.diligrp.upay.shared.domain.ContainerSupport;

/**
 * 确认交易模型-适用于预授权交易
 */
public class Confirm extends ContainerSupport {
    // 交易账户ID
    private Long accountId;
    // 操作金额
    private Long amount;
    // 支付密码
    private String password;

    public static Confirm of(Long accountId, Long amount, String password) {
        Confirm confirm = new Confirm();
        confirm.accountId = accountId;
        confirm.amount = amount;
        confirm.password = password;
        return confirm;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPassword() {
        return password;
    }
}
