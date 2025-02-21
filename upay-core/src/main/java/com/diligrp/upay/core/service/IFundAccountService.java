package com.diligrp.upay.core.service;

import com.diligrp.upay.core.model.FundAccount;

import java.util.List;

/**
 * 资金账户服务接口
 */
public interface IFundAccountService {
    /**
     * 根据账号ID查询资金账户
     */
    FundAccount findFundAccountById(Long accountId);

    /**
     * 悲观锁锁定资金账号防止数据并发修改
     */
    FundAccount lockFundAccountById(Long accountId);

    /**
     * 查询客户账户资金汇总信息
     */
    FundAccount sumCustomerFund(Long mchId, Long customerId);

    /**
     * 查询客户资金账户列表
     */
    List<FundAccount> listFundAccounts(Long mchId, Long customerId);
}
