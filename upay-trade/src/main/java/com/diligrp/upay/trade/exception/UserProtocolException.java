package com.diligrp.upay.trade.exception;

import com.diligrp.upay.core.exception.PaymentServiceException;

/**
 * 用户免密协议异常类
 */
public class UserProtocolException extends PaymentServiceException {

    // 已开通协议支付，但不允许使用
    public static final int USE_NOT_ALLOWED = 509001;
    // 未开通免密支付，允许开通
    public static final int OPEN_ALLOWED = 509002;
    // 未开通免密支付，不允许开通
    public static final int OPEN_NOT_ALLOWED = 509003;

    public UserProtocolException(String message) {
        super(message);
    }

    public UserProtocolException(int code, String message) {
        super(code, message);
    }

    public UserProtocolException(String message, Throwable ex) {
        super(message, ex);
    }
}
