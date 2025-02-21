package com.diligrp.upay.sentinel.model;

import com.diligrp.upay.core.exception.PaymentServiceException;
import com.diligrp.upay.sentinel.domain.Permission;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

/**
 * 账户风控配置模型
 */
public class UserPermission extends BaseDO {
    // 资金账号ID
    private Long accountId;
    // 权限值
    private Integer permission;
    // 充值风控配置
    private String deposit;
    // 提现风控配置
    private String withdraw;
    // 交易风控配置
    private String trade;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(String withdraw) {
        this.withdraw = withdraw;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public void checkPermission(Permission permission) {
        if(!Permission.hasPermission(this.permission, permission)) {
            String message = String.format("风控提示：账户无%s权限", permission.getName());
            throw new PaymentServiceException(ErrorCode.OPERATION_NOT_ALLOWED, message);
        }
    }

    public void checkPermissionForSeller(Permission permission) {
        if(!Permission.hasPermission(this.permission, permission)) {
            String message = String.format("风控提示：卖方账户无%s权限", permission.getName());
            throw new PaymentServiceException(ErrorCode.OPERATION_NOT_ALLOWED, message);
        }
    }

    public static Builder builder() {
        return new UserPermission().new Builder();
    }

    public class Builder {

        public Builder accountId(Long accountId) {
            UserPermission.this.accountId = accountId;
            return this;
        }

        public Builder permission(Integer permission) {
            UserPermission.this.permission = permission;
            return this;
        }

        public Builder deposit(String deposit) {
            UserPermission.this.deposit = deposit;
            return this;
        }

        public Builder withdraw(String withdraw) {
            UserPermission.this.withdraw = withdraw;
            return this;
        }

        public Builder trade(String trade) {
            UserPermission.this.trade = trade;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            UserPermission.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            UserPermission.this.modifiedTime = modifiedTime;
            return this;
        }

        public UserPermission build() {
            return UserPermission.this;
        }
    }
}
