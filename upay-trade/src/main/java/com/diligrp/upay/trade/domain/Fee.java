package com.diligrp.upay.trade.domain;

/**
 * 交易费用模型
 */
public class Fee {
    // 金额-元
    private Long amount;
    // 费用类型
    private Integer type;
    // 费用类型名称
    private String typeName;
    // 费用用途 - 买家/卖家
    private Integer useFor;
    // 费用描述
    private String description;

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getUseFor() {
        return useFor;
    }

    public void setUseFor(Integer useFor) {
        this.useFor = useFor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}