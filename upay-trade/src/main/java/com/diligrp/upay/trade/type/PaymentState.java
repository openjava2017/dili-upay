package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 支付状态列表
 */
public enum PaymentState implements IEnumType {

    PENDING("待支付", 1),

    PROCESSING("支付中", 2),

    SUCCESS("支付成功", 3),

    FAILED("支付失败", 4);

    private final String name;
    private final int code;

    PaymentState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<PaymentState> getState(int code) {
        Stream<PaymentState> STATES = Arrays.stream(PaymentState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<PaymentState> STATES = Arrays.stream(PaymentState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code).map(PaymentState::getName).findFirst();
        return result.orElse(null);
    }

    public static List<PaymentState> getStates() {
        return Arrays.asList(PaymentState.values());
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
