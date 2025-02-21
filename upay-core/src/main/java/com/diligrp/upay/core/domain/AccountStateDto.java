package com.diligrp.upay.core.domain;

import java.time.LocalDateTime;

/**
 * 账户状态数据传输对象
 */
public class AccountStateDto {
    // 客户ID
    private Long customerId;
    // 资金账号ID
    private Long accountId;
    // 账号状态
    private Integer state;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public static AccountStateDto of(Long accountId, Integer state, LocalDateTime modifiedTime, Integer version) {
        AccountStateDto accountState = new AccountStateDto();
        accountState.setAccountId(accountId);
        accountState.setState(state);
        accountState.setModifiedTime(modifiedTime);
        accountState.setVersion(version);
        return accountState;
    }

    public static AccountStateDto of(Long customerId, Integer state, LocalDateTime modifiedTime) {
        AccountStateDto accountState = new AccountStateDto();
        accountState.setCustomerId(customerId);
        accountState.setState(state);
        accountState.setModifiedTime(modifiedTime);
        return accountState;
    }
}
