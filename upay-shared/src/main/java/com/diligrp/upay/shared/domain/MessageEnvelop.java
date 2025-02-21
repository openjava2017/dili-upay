package com.diligrp.upay.shared.domain;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.exception.PlatformServiceException;
import com.diligrp.upay.shared.security.RsaCipher;
import com.diligrp.upay.shared.util.AssertUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * 消息信封模型将业务数据与签名数据进行分离
 * 用于接口数据安全校验：数据签名验签，防止数据在传输过程中被篡改
 */
public class MessageEnvelop {

    /**
     * 消息接收者：请求的服务点标识
     * 格式：componentId:methodName
     */
    private String recipient;

    /**
     * 签名数据-BASE64编码
     */
    private String signature;

    /**
     * 业务数据
     */
    private String payload;

    /**
     * 业务数据字符流的编码格式
     */
    private Charset charset;

    /**
     * 数据信封状态
     */
    private Integer state = 0;

    /**
     * 数据封包，根据我方的私钥进行数据签名
     *
     * @param privateKey - Base64编码的私钥字符串
     */
    public void packEnvelop(String privateKey) {
        AssertUtils.notEmpty(privateKey, "privateKey missed");
        AssertUtils.notEmpty(recipient, "recipient missed");
        AssertUtils.notEmpty(payload, "payload missed");

        try {
            byte[] data = String.format("%s\n%s", recipient, payload).getBytes(getCharset());
            PrivateKey secretKey = RsaCipher.getPrivateKey(privateKey);
            byte[] sign = RsaCipher.sign(data, secretKey);
            this.signature = Base64.getEncoder().encodeToString(sign);
        } catch (Exception ex) {
            throw new PlatformServiceException("支付数据封包失败: 签名失败", ex);
        }
    }

    /**
     * 数据拆包，根据对方的公钥进行数据验签
     *
     * @param publicKey - Base64编码的公钥字符串
     */
    public void unpackEnvelop(String publicKey) {
        AssertUtils.notEmpty(publicKey, "publicKey missed");
        AssertUtils.notEmpty(recipient, "recipient missed");
        AssertUtils.notEmpty(payload, "payload missed");
        AssertUtils.notEmpty(signature, "signature missed");

        try {
            byte[] data = String.format("%s\n%s", recipient, payload).getBytes(getCharset());
            byte[] sign = Base64.getDecoder().decode(signature);
            PublicKey secretKey = RsaCipher.getPublicKey(publicKey);
            boolean result = RsaCipher.verify(data, sign, secretKey);
            if (!result) {
                throw new PlatformServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "支付数据拆包失败：验签失败");
            }
        } catch (Exception ex) {
            throw new PlatformServiceException("支付数据拆包异常", ex);
        }
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Charset getCharset() {
        return charset == null ? StandardCharsets.UTF_8 : charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public static MessageEnvelop of(String recipient, String payload) {
        return MessageEnvelop.of(recipient, payload, null, null);
    }

    public static MessageEnvelop of(String recipient, String payload, String signature) {
        return MessageEnvelop.of(recipient, payload, signature, StandardCharsets.UTF_8);
    }

    public static MessageEnvelop of(String recipient, String payload, String signature, Charset charset) {
        MessageEnvelop envelop = new MessageEnvelop();
        envelop.setRecipient(recipient);
        envelop.setPayload(payload);
        envelop.setSignature(signature);
        envelop.setCharset(charset);
        envelop.setState(0);
        return envelop;
    }
}