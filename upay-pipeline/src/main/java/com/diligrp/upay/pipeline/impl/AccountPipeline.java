package com.diligrp.upay.pipeline.impl;

import com.diligrp.upay.core.domain.Configuration;
import com.diligrp.upay.core.domain.CoreAccount;
import com.diligrp.upay.core.domain.FundActivity;
import com.diligrp.upay.core.domain.FundTransaction;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.util.Configurer;
import com.diligrp.upay.core.util.TransactionBuilder;
import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.AssertUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户/余额支付通道
 */
public class AccountPipeline {
    private final CoreAccount account;

    private AccountPipeline(CoreAccount account) {
        this.account = account;
    }

    public static AccountPipeline of(UserAccount account) {
        return new AccountPipeline(new CoreAccount(account.getAccountId(), account.getParentId()));
    }

    public static AccountPipeline of(Long accountId) {
        return new AccountPipeline(new CoreAccount(accountId, 0L));
    }

    public TransactionBuilder openTransaction(String paymentId, int tradeType, LocalDateTime when) {
        return new Builder(paymentId, tradeType, when);
    }

    private class Builder implements TransactionBuilder {
        // 支付ID
        private final String paymentId;
        // 业务类型 - 资金冻结时不使用
        private final int tradeType;
        // 冻结金额 - 正数时为资金冻结, 负数时为资金解冻
        private long frozenAmount = 0;
        // 资金流
        private final List<FundActivity> funds = new ArrayList<>();
        // 配置
        private final Configuration.Builder builder;
        // 发生时间
        private final LocalDateTime when;
        public Builder(String paymentId, int tradeType, LocalDateTime when) {
            this.paymentId = paymentId;
            this.tradeType = tradeType;
            this.when = when;
            this.builder = Configuration.builder();
        }

        @Override
        public TransactionBuilder income(long amount, int type, String typeName, String description) {
            AssertUtils.isTrue(amount >= 0, "Invalid amount");
            if (amount > 0) {
                funds.add(FundActivity.of(amount, type, typeName, description));
            }
            return this;
        }

        @Override
        public TransactionBuilder outgo(long amount, int type, String typeName, String description) {
            AssertUtils.isTrue(amount >= 0, "Invalid amount");
            if (amount > 0) {
                funds.add(FundActivity.of(-amount, type, typeName, description));
            }
            return this;
        }

        @Override
        public TransactionBuilder freeze(long amount) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            this.frozenAmount += amount;
            return this;
        }

        @Override
        public TransactionBuilder unfreeze(long amount) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            this.frozenAmount -= amount;
            return this;
        }

        @Override
        public TransactionBuilder configure(Configurer<Configuration.Builder> configurer) {
            configurer.configure(builder);
            return this;
        }

        @Override
        public FundTransaction build() {
            if (!funds.isEmpty()) {
                AssertUtils.notEmpty(paymentId, "paymentId missed");
            } else if (frozenAmount == 0) {
                throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效资金事务");
            }

            Configuration configuration = builder.build();
            FundActivity[] fundActivities = funds.toArray(new FundActivity[0]);
            return FundTransaction.of(account, paymentId, tradeType, frozenAmount, fundActivities, configuration, when);
        }
    }
}
