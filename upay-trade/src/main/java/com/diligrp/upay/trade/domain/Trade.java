package com.diligrp.upay.trade.domain;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.shared.domain.ContainerSupport;

public class Trade extends ContainerSupport {
    // 账号ID
    private Long accountId;
    // 金额-分
    private Long amount;
    // 外部流水号
    private String outTradeNo;
    // 商品描述
    private String goods;
    // 备注
    private String description;

    public static final Trade of(Long accountId, Long amount, String outTradeNo, String goods, String description) {
        Trade trade = new Trade();
        trade.accountId = accountId;
        trade.amount = amount;
        trade.outTradeNo = outTradeNo;
        trade.goods = goods;
        trade.description = description;
        return trade;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getGoods() {
        return goods;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationPermit getApplication() {
        return getObject(ApplicationPermit.class);
    }
}
