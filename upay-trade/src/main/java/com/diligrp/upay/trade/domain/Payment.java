package com.diligrp.upay.trade.domain;

import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.shared.domain.ContainerSupport;

/**
 * 交易支付模型
 */
public class Payment extends ContainerSupport {
    // 付款账号ID
    private Long accountId;
    // 支付渠道
    private Integer channelId;
    // 支付方式
    private Integer payType;
    // 支付金额
    private Long amount;
    // 支付密码
    private String password;
    // 对账周期编号
    private String cycleNo;

    public static Payment of(Long accountId, Integer channelId, Integer payType, Long amount, String password, String cycleNo) {
        Payment payment = new Payment();
        payment.accountId = accountId;
        payment.channelId = channelId;
        payment.payType = payType;
        payment.amount = amount;
        payment.password = password;
        payment.cycleNo = cycleNo;
        return payment;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public Integer getPayType() {
        return payType;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPassword() {
        return password;
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public MerchantPermit getMerchant() {
        return getObject(MerchantPermit.class);
    }
}
