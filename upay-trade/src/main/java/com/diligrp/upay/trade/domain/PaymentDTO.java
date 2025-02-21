package com.diligrp.upay.trade.domain;

import java.util.List;
import java.util.Optional;

/**
 * 支付申请模型
 */
public class PaymentDTO {
    // 付款账号ID
    private Long accountId;
    // 支付渠道
    private Integer channelId;
    // 支付方式
    private Integer payType;
    // 支付密码
    private String password;
    // 费用列表
    private List<Fee> fees;
    // 免密协议号
    private Long protocolId;
    // 对账周期编号
    private String cycleNo;
    // 渠道信息
    private Channel channel;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }

    public Optional<Channel> channel() {
        return Optional.ofNullable(channel);
    }
}
