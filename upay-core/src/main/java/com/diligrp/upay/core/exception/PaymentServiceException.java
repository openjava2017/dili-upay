package com.diligrp.upay.core.exception;

import com.diligrp.upay.shared.exception.PlatformServiceException;

/**
 * 支付服务异常类
 */
public class PaymentServiceException extends PlatformServiceException {

    public PaymentServiceException(String message) {
        super(message);
    }

    public PaymentServiceException(int code, String message) {
        super(code, message);
    }

    public PaymentServiceException(String message, Throwable ex) {
        super(message, ex);
    }
}
