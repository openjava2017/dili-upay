package com.diligrp.upay.shared;

/**
 * 系统错误码列表 - 错误码前三位用于区分模块
 */
public class ErrorCode {
    // 系统未知异常
    public static final int SYSTEM_UNKNOWN_ERROR = 500000;
    // 无效参数错误
    public static final int ILLEGAL_ARGUMENT_ERROR = 500001;
    // 操作不允许
    public static final int OPERATION_NOT_ALLOWED = 500002;
    // 数据并发修改
    public static final int SYSTEM_BUSY_ERROR = 500003;
    // 对象不存在
    public static final int OBJECT_NOT_FOUND = 500004;
    // 对象已存在
    public static final int OBJECT_ALREADY_EXISTS = 500005;
    // 服务访问失败
    public static final int SERVICE_ACCESS_FAILED = 501001;
    // 用户密码不正确
    public static final int INVALID_USER_PASSWORD = 502001;
    // 无效对象状态
    public static final int INVALID_OBJECT_STATE = 502002;
    // 账户余额不足
    public static final int INSUFFICIENT_ACCOUNT_FUND = 503004;

    public static final String MESSAGE_UNKNOWN_ERROR = "支付系统未知异常，请联系系统管理员";

    public static final String MESSAGE_SYSTEM_BUSY = "支付系统正忙，请稍后重试";
}
