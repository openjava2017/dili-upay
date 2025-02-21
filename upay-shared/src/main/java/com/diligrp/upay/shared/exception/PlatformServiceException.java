package com.diligrp.upay.shared.exception;

import com.diligrp.upay.shared.ErrorCode;

/**
 * 所有模块异常类的基类
 */
public class PlatformServiceException extends RuntimeException {
    /**
     * 错误码
     */
    private int code = ErrorCode.SYSTEM_UNKNOWN_ERROR;

    /**
     * 是否打印异常栈
     */
    private boolean stackTrace = true;

    public PlatformServiceException(String message) {
        super(message);
    }

    public PlatformServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public PlatformServiceException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    public Throwable fillInStackTrace() {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode() {
        return code;
    }
}
