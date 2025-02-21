package com.diligrp.upay.core.domain;

public class CoreAccount {
    // 资金账号
    private Long accountId;
    // 父账号
    private Long parentId;

    public CoreAccount(Long accountId, Long parentId) {
        this.accountId = accountId;
        this.parentId = parentId;
    }

    public Long getMasterAccountId() {
        return parentId == 0 ? accountId : parentId;
    }

    public Long getChildAccountId() {
        return parentId == 0 ? null : accountId;
    }
}
