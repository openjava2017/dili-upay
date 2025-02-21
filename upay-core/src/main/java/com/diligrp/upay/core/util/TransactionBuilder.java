package com.diligrp.upay.core.util;

import com.diligrp.upay.core.domain.Configuration;
import com.diligrp.upay.core.domain.FundTransaction;

/**
 * 资金事务接口
 */
public interface TransactionBuilder {

    /**
     * 根据金额判断是资金收入还是资金支出
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     */
    default TransactionBuilder consume(long amount, int type, String typeName, String description) {
        if (amount > 0) {
            income(amount, type, typeName, description);
        } else if (amount < 0) {
            outgo(amount, type, typeName, description);
        }
        return this;
    }

    /**
     * 资金收入
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     * @param description - 费用描述
     */
    TransactionBuilder income(long amount, int type, String typeName, String description);

    /**
     * 资金支出
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     * @param description - 费用描述
     */
    TransactionBuilder outgo(long amount, int type, String typeName, String description);

    /**
     * 资金冻结
     *
     * @param amount - 操作金额
     */
    TransactionBuilder freeze(long amount);

    /**
     * 资金解冻
     *
     * @param amount - 操作金额
     */
    TransactionBuilder unfreeze(long amount);

    /**
     * 配置资金事务
     */
    TransactionBuilder configure(Configurer<Configuration.Builder> configurer);

    /**
     * 获取资金事务
     */
    FundTransaction build();
}
