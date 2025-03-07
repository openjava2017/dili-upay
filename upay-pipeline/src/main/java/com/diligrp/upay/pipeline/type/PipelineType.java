package com.diligrp.upay.pipeline.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支持的支付通道
 */
public enum PipelineType implements IEnumType {
    DIRECT_BANK("银企直连通道", 1),

    ONLINE_BANK("聚合支付通道", 2),

    WECHAT_PAY("微信支付通道", 10),

    ALI_PAY("支付宝通道", 11);

    private final String name;
    private final int code;

    PipelineType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<PipelineType> getType(int code) {
        Stream<PipelineType> TYPES = Arrays.stream(PipelineType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<PipelineType> TYPES = Arrays.stream(PipelineType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code).map(PipelineType::getName).findFirst();
        return result.orElse(null);
    }

    public static Map<Integer, String> getTypeNameMap() {
        return Arrays.stream(PipelineType.values()).collect(Collectors.toMap(PipelineType::getCode, PipelineType::getName));
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
