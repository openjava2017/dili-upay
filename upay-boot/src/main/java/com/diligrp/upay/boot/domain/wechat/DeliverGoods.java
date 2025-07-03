package com.diligrp.upay.boot.domain.wechat;

/**
 * 微信发货申请
 *
 * 解决买家微信付款成功，卖家微信商户号无法收到钱，需要去自己的微信后台操作一下【发货】
 */
public class DeliverGoods {
    // 支付订单号
    private String paymentId;
    // 物流模式
    private Integer logisticsType;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getLogisticsType() {
        return logisticsType;
    }

    public void setLogisticsType(Integer logisticsType) {
        this.logisticsType = logisticsType;
    }
}
