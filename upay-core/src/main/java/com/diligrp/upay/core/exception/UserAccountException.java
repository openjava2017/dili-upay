package com.diligrp.upay.core.exception;

import java.util.function.Supplier;

/**
 * 资金账号异常类
 */
public class UserAccountException extends PaymentServiceException {
    public UserAccountException(String message) {
        super(message);
    }

    public UserAccountException(int code, String message) {
        super(code, message);
    }

    public UserAccountException(String message, Throwable ex) {
        super(message, ex);
    }

    public static Supplier<UserAccountException> of(int errorCode, String message) {
        return () -> new UserAccountException(errorCode, message);
    }
}
