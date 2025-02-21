package com.diligrp.upay.shared.domain;

import com.diligrp.upay.shared.ErrorCode;

public class Message<T> {
    protected static final int CODE_SUCCESS = 200;
    protected static final int CODE_FAILURE = ErrorCode.SYSTEM_UNKNOWN_ERROR;
    protected static final String MSG_SUCCESS = "success";

    private Integer code;
    private String message;
    private T data;

    public Message() {
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Message<?> success() {
        Message<?> result = new Message();
        result.code = 200;
        result.message = "success";
        return result;
    }

    public static <E> Message<E> success(E data) {
        Message<E> result = new Message();
        result.code = 200;
        result.data = data;
        result.message = "success";
        return result;
    }

    public static Message<?> failure(String message) {
        Message<?> result = new Message();
        result.code = 1000;
        result.message = message;
        return result;
    }

    public static Message<?> failure(int code, String message) {
        Message<?> result = new Message();
        result.code = code;
        result.message = message;
        return result;
    }
}