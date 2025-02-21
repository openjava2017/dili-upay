package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 免密支付协议类型
 */
public enum ProtocolType implements IEnumType {
    ENTRY_FEE("进门缴费", 30),

    LOCAL_DELIVER("本地配送", 40),

    EXIT_FEE("出门缴费", 50),

    CREDIT_FEE("赊销缴费", 60),

    ETC_FEE("ETC缴费", 70);

    private final String name;
    private final int code;

    ProtocolType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<ProtocolType> getType(int code) {
        Stream<ProtocolType> TYPES = Arrays.stream(ProtocolType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ProtocolType> STATES = Arrays.stream(ProtocolType.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
            .map(ProtocolType::getName).findFirst();
        return result.orElse(null);
    }

    public static List<ProtocolType> getTypes() {
        return Arrays.asList(ProtocolType.values());
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
