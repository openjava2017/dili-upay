package com.diligrp.upay.sentinel.domain;

public class PasswordDTO {
    // 账号ID
    private Long accountId;

    // 密码
    private String password;

    // 新密码
    private String newPassword;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
