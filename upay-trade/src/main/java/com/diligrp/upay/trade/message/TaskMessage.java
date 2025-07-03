package com.diligrp.upay.trade.message;

/**
 * 异步消息模型
 */
public class TaskMessage {
    // 10分钟后处理未支付的微信预支付订单
    public static final int TYPE_WECHAT_PREPAY_SCAN = 10;
    // 10分钟后处理未完成的微信退款订单
    public static final int TYPE_WECHAT_REFUND_SCAN = 11;

    // 消息类型
    private int type;
    // 消息体
    private String payload;
    // 消息参数
    private String params;

    public int getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public String getParams() {
        return params;
    }

    public static TaskMessage of(int type, String payload) {
        return of(type, payload, null);
    }

    public static TaskMessage of(int type, String payload, String params) {
        TaskMessage taskMessage = new TaskMessage();
        taskMessage.type = type;
        taskMessage.payload = payload;
        taskMessage.params = params;
        return taskMessage;
    }
}
