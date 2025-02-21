package com.diligrp.upay.trade.model;

import com.diligrp.upay.shared.model.BaseDO;

import java.time.LocalDateTime;

/**
 * 支付费用数据模型
 */
public class PaymentFee extends BaseDO {
    // 支付ID
    private String paymentId;
    // 费用用途
    private Integer useFor;
    // 金额-分
    private Long amount;
    // 费用类型
    private Integer type;
    // 类型说明
    private String typeName;
    // 费用描述
    private String description;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getUseFor() {
        return useFor;
    }

    public void setUseFor(Integer useFor) {
        this.useFor = useFor;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static PaymentFee of(String paymentId, Long amount, Integer type, String typeName,
                                String description, LocalDateTime when) {
        return of(paymentId, amount, type, typeName, null, description, when);
    }

    public static PaymentFee of(String paymentId, Long amount, Integer type, String typeName,
                                Integer useFor, String description, LocalDateTime when) {
        PaymentFee fee = new PaymentFee();
        fee.setPaymentId(paymentId);
        fee.setAmount(amount);
        fee.setType(type);
        fee.setTypeName(typeName);
        fee.setUseFor(useFor);
        fee.setDescription(description);
        fee.setCreatedTime(when);
        return fee;
    }
}
