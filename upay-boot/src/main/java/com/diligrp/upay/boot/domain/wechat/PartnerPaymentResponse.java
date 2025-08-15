package com.diligrp.upay.boot.domain.wechat;

/**
 * 交易结果模型 - 服务商模式
 */
public class PartnerPaymentResponse {
    // 微信订单号
    private String transaction_id;
    // 商户订单号
    private String out_trade_no;
    // 交易状态
    private String trade_state;
    // 交易状态描述
    private String trade_state_desc;
    // 支付完成时间
    private String success_time;
    // 支付方
    private Payer payer;

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTrade_state() {
        return trade_state;
    }

    public void setTrade_state(String trade_state) {
        this.trade_state = trade_state;
    }

    public String getTrade_state_desc() {
        return trade_state_desc;
    }

    public void setTrade_state_desc(String trade_state_desc) {
        this.trade_state_desc = trade_state_desc;
    }

    public String getSuccess_time() {
        return success_time;
    }

    public void setSuccess_time(String success_time) {
        this.success_time = success_time;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public static class Payer {
        // 服务商下用户OpenId
        private String sp_openid;
        // 子商户下用户OpenId
        private String sub_openid;

        public String getSp_openid() {
            return sp_openid;
        }

        public void setSp_openid(String sp_openid) {
            this.sp_openid = sp_openid;
        }

        public String getSub_openid() {
            return sub_openid;
        }

        public void setSub_openid(String sub_openid) {
            this.sub_openid = sub_openid;
        }
    }
}
