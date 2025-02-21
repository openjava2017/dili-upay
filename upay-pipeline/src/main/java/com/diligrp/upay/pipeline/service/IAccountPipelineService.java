package com.diligrp.upay.pipeline.service;

import com.diligrp.upay.core.domain.FundTransaction;
import com.diligrp.upay.core.domain.TransactionStatus;

import java.util.function.Supplier;

public interface IAccountPipelineService {

    /**
     * 提交资金事务, 乐观锁实现
     */
    TransactionStatus submit(Supplier<FundTransaction> supplier);

    /**
     * 提交资金事务, 悲观锁实现
     */
    TransactionStatus submitExclusively(Supplier<FundTransaction> supplier);
}
