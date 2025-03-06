package com.diligrp.upay.pipeline.util;

/**
 * 微信支付平台常量列表
 */
public class WechatConstants {
    // 签名算法 - 请求时签名使用
    public static final String SIGN_ALGORITHM = "SHA256WithRSA";
    // 密钥算法 - 请求时签名使用
    public static final String RSA_ALGORITHM = "RSA";
    // 加密算法 - 证书和回调报文解密使用
    public static final String AES_ALGORITHM = "AES";
    // 加密算法 - 证书和回调报文解密使用
    public static final String AESGCM_ALGORITHM = "AES/GCM/NoPadding";
    // 密钥长度 - 证书和回调报文解密使用
    public static final int KEY_LENGTH_BYTE = 32;
    public static final int TAG_LENGTH_BIT = 128;

    // HTTP常量
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";

    // 微信平台HTTP请求常量
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String USER_AGENT = "DiliPay-HttpClient/2.4.0 Java/11.0.6";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String ACCEPT_JSON = "application/json";

    // 微信平台HTTP响应常量
    public static final String HEADER_TIMESTAMP = "Wechatpay-Timestamp";
    public static final String HEADER_NONCE = "Wechatpay-Nonce";
    public static final String HEADER_SERIAL_NO = "Wechatpay-Serial";
    public static final String HEADER_SIGNATURE = "Wechatpay-Signature";

    public static String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+08:00";
    public static String NOTIFY_EVENT_TYPE = "TRANSACTION.SUCCESS";
    public static String REFUND_EVENT_TYPE = "REFUND.SUCCESS";

    // 公众号支付
    public static String PAY_JSAPI = "JSAPI";
    // 扫码支付
    public static String PAY_NATIVE = "NATIVE";
    // App支付
    public static String PAY_APP = "App";
    // 付款码支付
    public static String PAY_MICROPAY = "MICROPAY";
    // H5支付
    public static String PAY_MWEB = "MWEB";
    // 刷脸支付
    public static String PAY_FACEPAY = "FACEPAY";


    // 支付状态常量列表
    public static final String STATE_SUCCESS = "SUCCESS"; // 支付成功
    public static final String STATE_REFUND = "REFUND"; // 转入退款
    public static final String STATE_NOTPAY = "NOTPAY"; // 未支付
    public static final String STATE_CLOSED = "CLOSED"; // 已关闭
    public static final String STATE_REVOKED = "REVOKED"; // 已撤销（付款码支付）
    public static final String STATE_USERPAYING = "USERPAYING"; // 用户支付中（付款码支付）
    public static final String STATE_PAYERROR = "PAYERROR"; // 支付失败(其他原因，如银行返回失败)

    // 退款状态常量列表
    public static final String REFUND_SUCCESS = "SUCCESS"; // 退款成功
    public static final String REFUND_CLOSED = "CLOSED"; // 退款关闭
    public static final String REFUND_PROCESSING = "PROCESSING"; // 退款处理中
    public static final String REFUND_ABNORMAL = "ABNORMAL"; // 退款异常


}
