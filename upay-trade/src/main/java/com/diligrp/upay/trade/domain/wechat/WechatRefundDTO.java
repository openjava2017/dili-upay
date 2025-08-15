package com.diligrp.upay.trade.domain.wechat;

import com.diligrp.upay.trade.domain.Fee;

import java.util.List;
import java.util.Optional;

/**
 * 微信退款申请
 */
public class WechatRefundDTO {
    // 原支付号
    private String paymentId;
    // 退款金额
    private Long amount;
    // 费用列表
    private List<Fee> fees;
    // 对账周期编号
    private String cycleNo;
    // 回调地址
    private String notifyUri;
    // 退款原因
    private String description;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
    }

    public String getNotifyUri() {
        return notifyUri;
    }

    public void setNotifyUri(String notifyUri) {
        this.notifyUri = notifyUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
