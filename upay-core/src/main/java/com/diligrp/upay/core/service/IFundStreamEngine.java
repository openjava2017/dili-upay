package com.diligrp.upay.core.service;

import com.diligrp.upay.core.domain.FundTransaction;
import com.diligrp.upay.core.domain.TransactionStatus;

/**
 * 资金流引擎接口
 */
public interface IFundStreamEngine {
    /**
     * 提交资金事务: 数据库乐观锁操作资金余额和添加资金流水、冻结和解冻资金
     */
    TransactionStatus submit(FundTransaction transaction);


    /**
     * 提交资金事务：使用数据库悲观锁防止数据并发修改
     */
    TransactionStatus submitExclusively(FundTransaction transaction);
}
