package com.diligrp.upay.boot.domain.wechat;

/**
 * 退款结果模型
 */
public class NotifyRefundResponse {
    // 微信退款单号
    private String refund_id;
    // 商户退款单号
    private String out_refund_no;
    // 原支付交易对应的商户订单号
    private String out_trade_no;
    // 退款状态 - 退款结果通知返回
    private String refund_status;
    // 退款完成时间
    private String success_time;

    public String getRefund_id() {
        return refund_id;
    }

    public void setRefund_id(String refund_id) {
        this.refund_id = refund_id;
    }

    public String getOut_refund_no() {
        return out_refund_no;
    }

    public void setOut_refund_no(String out_refund_no) {
        this.out_refund_no = out_refund_no;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getRefund_status() {
        return refund_status;
    }

    public void setRefund_status(String refund_status) {
        this.refund_status = refund_status;
    }

    public String getSuccess_time() {
        return success_time;
    }

    public void setSuccess_time(String success_time) {
        this.success_time = success_time;
    }
}
