package com.diligrp.upay.pipeline.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 微信支付方式
 */
public enum WechatPaymentType implements IEnumType {
    JSAPI("JSAPI支付", 1),

    NATIVE("NATIVE支付", 2),

    APP("APP支付", 3),

    MICROPAY("付款码支付", 4),

    MWEB("H5支付", 5),

    FACEPAY("刷脸支付", 6);

    private final String name;
    private final int code;

    WechatPaymentType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<WechatPaymentType> getType(int code) {
        Stream<WechatPaymentType> TYPES = Arrays.stream(WechatPaymentType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<WechatPaymentType> TYPES = Arrays.stream(WechatPaymentType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code).map(WechatPaymentType::getName).findFirst();
        return result.orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
