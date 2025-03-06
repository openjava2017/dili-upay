package com.diligrp.upay.pipeline.domain.wechat;

/**
 * 微信发货申请
 *
 * 解决买家微信付款成功，卖家微信商户号无法收到钱，需要去自己的微信后台操作一下【发货】
 */
public class UploadShippingRequest {
    // 微信订单号
    private String transactionId;
    // 物流模式
    private Integer logisticsType;
    // 商品信息
    private String goods;
    // 支付人标识
    private String openId;

    public static UploadShippingRequest of(String transactionId, Integer logisticsType, String goods, String openId) {
        UploadShippingRequest request = new UploadShippingRequest();
        request.transactionId = transactionId;
        request.logisticsType = logisticsType;
        request.goods = goods;
        request.openId = openId;
        return request;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Integer getLogisticsType() {
        return logisticsType;
    }

    public String getGoods() {
        return goods;
    }

    public String getOpenId() {
        return openId;
    }
}
