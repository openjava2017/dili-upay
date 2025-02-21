package com.diligrp.upay.trade.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

/**
 * 资金冻结订单数据模型
 */
public class FrozenOrder extends BaseDO {
    // 支付ID
    private String paymentId;
    // 账号ID
    private Long accountId;
    // 子账号
    private Long childId;
    // 用户名
    private String name;
    // 冻结类型-系统冻结 交易冻结
    private Integer type;
    // 金额-分
    private Long amount;
    // 状态-冻结 解冻
    private Integer state;
    // 扩展信息
    private String extension;
    // 备注
    private String description;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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


    public static Builder builder() {
        return new FrozenOrder().new Builder();
    }

    public class Builder {

        public Builder paymentId(String paymentId) {
            FrozenOrder.this.paymentId = paymentId;
            return this;
        }

        public Builder accountId(Long accountId) {
            FrozenOrder.this.accountId = accountId;
            return this;
        }

        public Builder childId(Long childId) {
            FrozenOrder.this.childId = childId;
            return this;
        }

        public Builder name(String name) {
            FrozenOrder.this.name = name;
            return this;
        }

        public Builder type(Integer type) {
            FrozenOrder.this.type = type;
            return this;
        }

        public Builder amount(Long amount) {
            FrozenOrder.this.amount = amount;
            return this;
        }

        public Builder state(Integer state) {
            FrozenOrder.this.state = state;
            return this;
        }

        public Builder extension(String extension) {
            FrozenOrder.this.extension = extension;
            return this;
        }

        public Builder description(String description) {
            FrozenOrder.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            FrozenOrder.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            FrozenOrder.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            FrozenOrder.this.modifiedTime = modifiedTime;
            return this;
        }

        public FrozenOrder build() {
            return FrozenOrder.this;
        }
    }
}
