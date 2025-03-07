package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 交易类型列表
 */
public enum TradeType implements IEnumType {

    DEPOSIT("存款", 10),

    WITHDRAW("取款", 11),

    PAY_FEE("缴费", 12),

    AUTH_FEE("预授权缴费", 13),

    REFUND_FEE("退费", 14),

    DIRECT_TRADE("即时交易", 20),

    TRANSFER("账户转账", 23),

    VOUCH_TRADE("担保交易", 24),

    VOUCH_SETTLE("担保结算", 25),

    ONLINE_DEPOSIT("在线存款", 30),

    ONLINE_WITHDRAW("在线取款", 31),

    ONLINE_FEE("在线缴费", 32),

    ONLINE_TRADE("在线交易", 33),

    REFUND_TRADE("交易退款", 41);

    private final String name;
    private final int code;

    TradeType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<TradeType> getType(int code) {
        Stream<TradeType> TYPES = Arrays.stream(TradeType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<TradeType> TYPES = Arrays.stream(TradeType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
            .map(TradeType::getName).findFirst();
        return result.orElse(null);
    }

    public static List<TradeType> getTypes() {
        return Arrays.asList(TradeType.values());
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
