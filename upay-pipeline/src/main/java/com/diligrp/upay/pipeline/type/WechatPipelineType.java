package com.diligrp.upay.pipeline.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 微信支付通道类型
 */
public enum WechatPipelineType implements IEnumType {
    DIRECT("直连通道", 1),

    PARTNER("服务商通道", 2);

    private final String name;
    private final int code;

    WechatPipelineType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<WechatPipelineType> getType(int code) {
        Stream<WechatPipelineType> TYPES = Arrays.stream(WechatPipelineType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<WechatPipelineType> TYPES = Arrays.stream(WechatPipelineType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code).map(WechatPipelineType::getName).findFirst();
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
