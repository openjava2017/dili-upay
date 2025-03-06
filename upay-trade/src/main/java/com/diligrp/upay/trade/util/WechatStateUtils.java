package com.diligrp.upay.trade.util;

import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.type.PaymentState;

public final class WechatStateUtils {
    public static PaymentState getPaymentState(String wechatState) {
        PaymentState state = switch (wechatState) {
            // 转入退款WechatConstants.STATE_REFUND也认为是支付成功，只有支付成功的才会转入STATE_REFUND状态
            // 目前支付系统发生退款时，不会修改原来支付记录upay_trade_payment，只会修改upay_trade_order
            case WechatConstants.STATE_SUCCESS, WechatConstants.STATE_REFUND -> PaymentState.SUCCESS;
            case WechatConstants.STATE_NOTPAY -> PaymentState.PENDING;
            case WechatConstants.STATE_USERPAYING -> PaymentState.PROCESSING;
            case WechatConstants.STATE_CLOSED, WechatConstants.STATE_REVOKED, WechatConstants.STATE_PAYERROR -> PaymentState.FAILED;
            default ->
                throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "未知的微信支付状态: " + wechatState);
        };
        return state;
    }

    public static PaymentState getRefundState(String wechatState) {
        PaymentState state = switch (wechatState) {
            // 转入退款WechatConstants.STATE_REFUND也认为是支付成功，只有支付成功的才会转入STATE_REFUND状态
            // 目前支付系统发生退款时，不会修改原来支付记录upay_trade_payment，只会修改upay_trade_order
            case WechatConstants.REFUND_SUCCESS -> PaymentState.SUCCESS;
            case WechatConstants.REFUND_PROCESSING -> PaymentState.PROCESSING;
            case WechatConstants.REFUND_CLOSED, WechatConstants.REFUND_ABNORMAL -> PaymentState.FAILED;
            default ->
                throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "未知的微信退款状态: " + wechatState);
        };
        return state;
    }
}
