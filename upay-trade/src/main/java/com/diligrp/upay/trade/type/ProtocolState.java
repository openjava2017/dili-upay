package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 免密支付协议状态
 */
public enum ProtocolState implements IEnumType {
    NORMAL("启用", 1),

    VOID("禁用", 2);

    private final String name;
    private final int code;

    ProtocolState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<ProtocolState> getState(int code) {
        Stream<ProtocolState> STATES = Arrays.stream(ProtocolState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ProtocolState> STATES = Arrays.stream(ProtocolState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code)
            .map(ProtocolState::getName).findFirst();
        return result.orElse(null);
    }

    public static List<ProtocolState> getStates() {
        return Arrays.asList(ProtocolState.values());
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
