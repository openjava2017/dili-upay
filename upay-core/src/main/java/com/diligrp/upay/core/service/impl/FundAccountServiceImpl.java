package com.diligrp.upay.core.service.impl;

import com.diligrp.upay.core.dao.IFundAccountDao;
import com.diligrp.upay.core.exception.UserAccountException;
import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.core.service.IFundAccountService;
import com.diligrp.upay.shared.ErrorCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 资金账户服务实现
 */
@Service("fundAccountService")
public class FundAccountServiceImpl implements IFundAccountService {

    @Resource
    private IFundAccountDao fundAccountDao;

    /**
     * {@inheritDoc}
     *
     * 乐观锁实现需Spring事务传播属性使用REQUIRES_NEW，数据库事务隔离级别READ_COMMITTED
     * 为了防止业务层事务的数据隔离级别和Mybatis的查询缓存干扰导致数据的重复读（无法读取到最新的数据记录），
     * 因此新启一个Spring事务（一个新数据库连接）并将数据隔离级别设置成READ_COMMITTED;
     * Mysql默认隔离级别为REPEATABLE_READ
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public FundAccount findFundAccountById(Long accountId) {
        Optional<FundAccount> fundOpt = fundAccountDao.findByAccountId(accountId);
        return fundOpt.orElseThrow(() -> new UserAccountException(ErrorCode.OBJECT_NOT_FOUND, "账号资金不存在"));
    }

    @Override
    public FundAccount lockFundAccountById(Long accountId) {
        Optional<FundAccount> fundOpt = fundAccountDao.lockByAccountId(accountId);
        return fundOpt.orElseThrow(() -> new UserAccountException(ErrorCode.OBJECT_NOT_FOUND, "账号资金不存在"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FundAccount sumCustomerFund(Long mchId, Long customerId) {
        Optional<FundAccount> fundOpt = fundAccountDao.sumCustomerFund(mchId, customerId);
        return fundOpt.orElseThrow(() -> new UserAccountException(ErrorCode.OBJECT_NOT_FOUND, "该客户无资金账号"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FundAccount> listFundAccounts(Long mchId, Long customerId) {
        return fundAccountDao.listFundAccounts(mchId, customerId);
    }
}