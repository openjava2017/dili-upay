package com.diligrp.upay.pipeline.type;

import com.diligrp.upay.shared.type.IEnumType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支持的渠道
 */
public enum ChannelType implements IEnumType {

    ACCOUNT("账户渠道", 1),

    CASH("现金渠道", 2),

    WXPAY("微信渠道", 10),

    ALIPAY("支付宝渠道", 11),

    ICBC("工商银行", 20),

    ABC("农业银行", 21),

    BOC("中国银行", 22),

    CCB("建设银行", 23),

    BCM("交通银行", 24),

    CITIC("中信银行", 25),

    CMB("招商银行", 27),

    SJBANK("盛京银行", 28),

    RCB("农商银行", 29),

    HZBANK("杭州银行", 30);

    private final String name;
    private final int code;

    ChannelType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<ChannelType> getType(int code) {
        Stream<ChannelType> TYPES = Arrays.stream(ChannelType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ChannelType> TYPES = Arrays.stream(ChannelType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code).map(ChannelType::getName).findFirst();
        return result.orElse(null);
    }

    public static Optional<ChannelType> getBankChannel(String bankCode) {
        Stream<ChannelType> TYPES = Arrays.stream(ChannelType.values());
        return TYPES.filter(type -> type.name().equalsIgnoreCase(bankCode)).findFirst();
    }

    public static Map<Integer, String> getTypeNameMap() {
        return Arrays.stream(ChannelType.values()).collect(Collectors.toMap(ChannelType::getCode, ChannelType::getName));
    }

    /**
     * 判断渠道是否可用于充值业务
     */
    public static boolean forDeposit(int code) {
        return code == CASH.getCode();
    }

    /**
     * 判断渠道是否可用于提现业务
     */
    public static boolean forWithdraw(int code) {
        return code == CASH.getCode();
    }

    /**
     * 判断渠道是否可用于缴费业务
     */
    public static boolean forFee(int code) {
        return code == CASH.getCode() || code == ACCOUNT.getCode();
    }

    /**
     * 判断渠道是否可用于"即时交易", "预授权交易"和"转账"业务
     */
    public static boolean forTrade(int code) {
        return code == ACCOUNT.getCode();
    }

    /**
     * 判断渠道是否可用于"预授权缴费"业务
     */
    public static boolean forPreAuthFee(int code) {
        return code == ACCOUNT.getCode();
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
