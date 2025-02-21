package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 交易状态列表
 */
public enum TradeState implements IEnumType {

    PENDING("待处理", 1),

    FROZEN("交易冻结", 2),

    SUCCESS("交易成功", 3),

    FAILED("交易失败", 4),

    REFUND("交易退款", 5),

    CLOSED("交易关闭", 6);

    private final String name;
    private final int code;

    TradeState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<TradeState> getState(int code) {
        Stream<TradeState> STATES = Arrays.stream(TradeState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<TradeState> STATES = Arrays.stream(TradeState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code).map(TradeState::getName).findFirst();
        return result.orElse(null);
    }

    public static List<TradeState> getStates() {
        return Arrays.asList(TradeState.values());
    }

    /**
     * 交易订单是否允许确认交易
     *
     * @param state - 交易订单状态
     * @return 是否允许确认交易
     */
    public static boolean forConfirm(int state) {
        return state == TradeState.FROZEN.getCode();
    }

    /**
     * 交易订单是否允许退款; 允许多次交易退款
     *
     * @param state - 交易订单状态
     * @return 是否允许交易退款
     */
    public static boolean forRefund(int state) {
        return state == TradeState.SUCCESS.getCode() || state == TradeState.REFUND.getCode();
    }

    /**
     * 交易订单是否允许撤销
     *
     * @param state - 交易订单状态
     * @return 是否允许撤销
     */
    public static boolean forCancel(int state) {
        return state == TradeState.FROZEN.getCode() || state == TradeState.SUCCESS.getCode();
    }

    /**
     * 交易订单是否允许交易冲正
     *
     * @param state - 交易订单状态
     * @return 是否允许交易冲正
     */
    public static boolean forCorrect(int state) {
        return state == TradeState.SUCCESS.getCode();
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
