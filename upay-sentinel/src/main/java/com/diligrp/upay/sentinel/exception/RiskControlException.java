package com.diligrp.upay.sentinel.exception;

import com.diligrp.upay.core.exception.PaymentServiceException;

/**
 * 支付风控异常类
 */
public class RiskControlException extends PaymentServiceException {
    public RiskControlException(String message) {
        super(message);
    }

    public RiskControlException(int code, String message) {
        super(code, message);
    }

    public RiskControlException(String message, Throwable ex) {
        super(message, ex);
    }
}
