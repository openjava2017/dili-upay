package com.diligrp.upay.trade.domain;

/**
 * 资金冻结数据传输模型
 */
public class FreezeFundDTO {
    // 资金账号ID
    private Long accountId;
    // 类型 - 系统冻结和交易冻结
    private Integer type;
    // 冻结金额 - 分
    private Long amount;
    // 扩展信息
    private String extension;
    // 描述
    private String description;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
