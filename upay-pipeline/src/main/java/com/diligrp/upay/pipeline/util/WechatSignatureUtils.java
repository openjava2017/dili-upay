package com.diligrp.upay.pipeline.util;

import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.shared.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 微信签名验签工具类
 */
public class WechatSignatureUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WechatSignatureUtils.class);

    // 常用字符常量
    private static String EMPTY_STR = "";
    private static String ENTER_STR = "\n";
    // 微信认证类型 - 请求时使用
    private static final String TOKEN_FORMAT = "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",serial_no=\"%s\",signature=\"%s\"";

    /**
     * 获取微信支付的认证信息和签名信息
     *
     * @param mchId - 服务商商户号
     * @param method - 请求方法: GET/POST/PUT/**
     * @param uri - 请求URL
     * @param payload - POST请求消息体
     * @param privateKey - Base64编码的私钥
     * @param keySerialNo - 商户API证书(公钥)序列号
     * @return 认证信息和签名信息，通过Http Header传输，格式 Authorization: 认证类型 签名信息
     * @throws Exception 签名异常
     */
    public static String authorization(String mchId, String method, String uri, String payload,
        PrivateKey privateKey, String keySerialNo) throws Exception {
        String nonce = RandomUtils.randomString(32);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        // No need handle uri here, the caller ensure the normal and valid URL
        // URL url = new URL("https://www.diligrp.com/ddd?aaa=x"); url.getPath() = /ddd url.getQuery() = "aa=x"
        String message = Stream.of(method, uri, timestamp, nonce, payload).map(o -> o == null ? EMPTY_STR : o)
            .collect(Collectors.joining(ENTER_STR, EMPTY_STR, ENTER_STR));
        LOG.debug("\n---Wechat Platform Sign Param---\n{}\n--------------------------------", message);
        String signature = signature(message, privateKey);
        return String.format(TOKEN_FORMAT, mchId, nonce, timestamp, keySerialNo, signature);
    }

    /**
     * @see WechatSignatureUtils#authorization(String, String, String, String, PrivateKey, String)
     */
    public static String authorization(String mchId, String method, String uri, PrivateKey privateKey,
        String keySerialNo) throws Exception {
        return authorization(mchId, method, uri, null, privateKey, keySerialNo);
    }

    /**
     * 微信支付签名
     *
     * @param message - 被签名字符串
     * @param privateKey - 服务商私钥
     * @return 签名数据
     * @throws Exception 签名异常
     */
    public static String signature(String message, PrivateKey privateKey) throws Exception {
        byte[] packet = message.getBytes(StandardCharsets.UTF_8);
        Signature signature = Signature.getInstance(WechatConstants.SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(packet);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 微信支付数据验签
     *
     * @param payload - POST请求消息体
     * @param timestamp - 时间戳
     * @param nonce - 随机字符串
     * @param sign - 签名数据
     * @param publicKey - 微信支付平台公钥
     * @return 是否验签成功
     * @throws Exception 验签异常
     */
    public static boolean verify(String payload, String timestamp, String nonce, String sign, PublicKey publicKey) throws Exception {
        byte[] signBytes = Base64.getDecoder().decode(sign);
        StringBuilder message = new StringBuilder();
        message.append(timestamp).append("\n").append(nonce).append("\n").append(payload).append("\n");
        byte[] packet = message.toString().getBytes(StandardCharsets.UTF_8);

        Signature signature = Signature.getInstance(WechatConstants.SIGN_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(packet);
        return signature.verify(signBytes);
    }

    /**
     * 报文解密, 证书和微信回调时使用
     *
     * @param payload - Base64编码的待解密的消息
     * @param nonce - 解密使用随机字符串
     * @param extraData - 解密使用的附加数据包, 可为空
     * @param apiV3Key - 微信平台apiV3Key
     * @return 消息明文
     * @throws Exception 解密异常
     */
    public static String decrypt(String payload, String nonce, String extraData, SecretKeySpec apiV3Key) throws Exception {
        byte[] packet = Base64.getDecoder().decode(payload);
        byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);

        Cipher cipher = Cipher.getInstance(WechatConstants.AESGCM_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(WechatConstants.TAG_LENGTH_BIT, nonceBytes);
        cipher.init(Cipher.DECRYPT_MODE, apiV3Key, spec);
        if (ObjectUtils.isNotEmpty(extraData)) {
            cipher.updateAAD(extraData.getBytes(StandardCharsets.UTF_8));
        }

        return new String(cipher.doFinal(packet), StandardCharsets.UTF_8);
    }
}
