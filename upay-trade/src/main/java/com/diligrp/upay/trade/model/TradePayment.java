package com.diligrp.upay.trade.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

/**
 * 交易支付数据模型
 */
public class TradePayment extends BaseDO {
    // 支付ID
    private String paymentId;
    // 交易ID
    private String tradeId;
    // 支付渠道
    private Integer channelId;
    // 支付方式
    private Integer payType;
    // 账号ID
    private Long accountId;
    // 账号名称
    private String name;
    // 金额-分
    private Long amount;
    // 费用金额-分
    private Long fee;
    // 免密支付协议ID
    private String protocolId;
    // 对账周期编号
    private String cycleNo;
    // 支付状态
    private Integer state;
    // 备注
    private String description;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Builder builder() {
        return new TradePayment().new Builder();
    }

    public class Builder {
        public Builder paymentId(String paymentId) {
            TradePayment.this.paymentId = paymentId;
            return this;
        }

        public Builder tradeId(String tradeId) {
            TradePayment.this.tradeId = tradeId;
            return this;
        }

        public Builder channelId(Integer channelId) {
            TradePayment.this.channelId = channelId;
            return this;
        }

        public Builder payType(Integer payType) {
            TradePayment.this.payType = payType;
            return this;
        }

        public Builder accountId(Long accountId) {
            TradePayment.this.accountId = accountId;
            return this;
        }

        public Builder name(String name) {
            TradePayment.this.name = name;
            return this;
        }

        public Builder amount(Long amount) {
            TradePayment.this.amount = amount;
            return this;
        }

        public Builder fee(Long fee) {
            TradePayment.this.fee = fee;
            return this;
        }

        public Builder protocolId(String protocolId) {
            TradePayment.this.protocolId = protocolId;
            return this;
        }

        public Builder cycleNo(String cycleNo) {
            TradePayment.this.cycleNo = cycleNo;
            return this;
        }

        public Builder state(Integer state) {
            TradePayment.this.state = state;
            return this;
        }

        public Builder description(String description) {
            TradePayment.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            TradePayment.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            TradePayment.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            TradePayment.this.modifiedTime = modifiedTime;
            return this;
        }

        public TradePayment build() {
            return TradePayment.this;
        }
    }
}
