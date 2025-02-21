package com.diligrp.upay.trade.type;

import com.diligrp.upay.shared.type.IEnumType;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.Fee;
import com.diligrp.upay.trade.model.PaymentFee;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 费用用途枚举类
 */
public enum FeeUseFor implements IEnumType {
    FOR_BUYER("买家费用", 1),

    FOR_SELLER("卖家费用", 2),

    FOR_DEDUCT("费用抵扣", 3);

    private final String name;
    private final int code;

    FeeUseFor(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<FeeUseFor> getType(int code) {
        Stream<FeeUseFor> TYPES = Arrays.stream(FeeUseFor.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<FeeUseFor> STATES = Arrays.stream(FeeUseFor.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
            .map(FeeUseFor::getName).findFirst();
        return result.orElse(null);
    }

    public static List<FeeUseFor> getTypes() {
        return Arrays.asList(FeeUseFor.values());
    }

    /**
     * 检查费用项买卖属性
     */
    public static void checkUseFor(Fee fee) {
        Integer useFor = fee.getUseFor();
        AssertUtils.isTrue(useFor == null || FeeUseFor.FOR_BUYER.equalTo(useFor) ||
            FeeUseFor.FOR_SELLER.equalTo(useFor), "invalid fee useFor");
    }

    public static boolean forBuyer(Fee fee) {
        Integer useFor = fee.getUseFor();
        if (useFor == null) { // 默认为买家费用
            return true;
        }
        return FOR_BUYER.equalTo(useFor);
    }

    public static boolean forBuyer(PaymentFee fee) {
        Integer useFor = fee.getUseFor();
        if (useFor == null) { // 默认为买家费用
            return true;
        }
        return FOR_BUYER.equalTo(useFor);
    }

    public static boolean forSeller(Fee fee) {
        Integer useFor = fee.getUseFor();
        if (useFor == null) {
            return false;
        }
        return FOR_SELLER.equalTo(useFor);
    }

    public static boolean forSeller(PaymentFee fee) {
        Integer useFor = fee.getUseFor();
        if (useFor == null) {
            return false;
        }
        return FOR_SELLER.equalTo(useFor);
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
