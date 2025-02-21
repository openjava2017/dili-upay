package com.diligrp.upay.core.service.impl;

import com.diligrp.upay.core.dao.IFundAccountDao;
import com.diligrp.upay.core.dao.IFundStatementDao;
import com.diligrp.upay.core.domain.CoreAccount;
import com.diligrp.upay.core.domain.FundActivity;
import com.diligrp.upay.core.domain.FundTransaction;
import com.diligrp.upay.core.domain.TransactionStatus;
import com.diligrp.upay.core.exception.UserAccountException;
import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.core.model.FundStatement;
import com.diligrp.upay.core.service.IFundAccountService;
import com.diligrp.upay.core.service.IFundStreamEngine;
import com.diligrp.upay.core.type.ActionType;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.shared.ErrorCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 核心资金流引擎实现
 */
@Service("fundStreamEngine")
public class DefaultFundStreamEngine implements IFundStreamEngine {

    private static final int RETRIES = 3;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundStatementDao fundStatementDao;

    /**
     * {@inheritDoc}
     *
     * 采用数据库乐观锁防止数据并发修改，且在发生数据并发修改时进行重试;
     * 1. 处理优先策略：资金解冻-资金交易（收入或支出）-资金冻结，此处理逻辑满足解冻并消费类业务（比如：预授权交易）
     * 或冻结并收入业务（先冻结后收入，冻结时异常提示余额不足），不会造成异常提示余额不足的情况
     * 2. 资金交易处理逻辑遵循收入优先，即收支明细先产生收入流水后产生支出流水；整个处理逻辑并不会影响余额校验，
     * 只是保证收支明细中期初余额不会出现负值（比如：余额为100，资金交易时先支出200后收入300）
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TransactionStatus submit(FundTransaction transaction) {
        boolean success = true;
        FundAccount fundAccount = null;
        TransactionStatus status = null;
        CoreAccount account = transaction.getAccount();

        for (int retry = 0; retry < RETRIES; retry ++) {
            // 新启事务查询账户资金及数据版本，避免数据库隔离级别和Mybatis缓存造成乐观锁重试机制无法生效
            fundAccount = fundAccountService.findFundAccountById(account.getMasterAccountId());
            status = TransactionStatus.of(fundAccount.getAccountId(), fundAccount.getBalance(), 0L,
                fundAccount.getFrozenAmount(), transaction.getFrozenAmount(), transaction.getWhen());
            // 处理解冻资金transaction.getFrozenAmount()<0
            if (transaction.isUnfrozenTransaction()) {
                // 判断冻结余额是否充足
                if (fundAccount.getFrozenAmount() + transaction.getFrozenAmount() < 0) {
                    throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户冻结余额不足");
                }
                fundAccount.setFrozenAmount(fundAccount.getFrozenAmount() + transaction.getFrozenAmount());
            }

            // 处理资金交易
            if (transaction.isFundTransaction()) {
                long totalAmount = Arrays.stream(transaction.getActivities()).mapToLong(FundActivity::getAmount).sum();
                long availableAmount = fundAccount.getBalance() - fundAccount.getFrozenAmount();
                // 如果为资金支出(totalAmount<0)则判断账户余额是否充足
                if (availableAmount + totalAmount < 0) {
                    throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户可用余额不足");
                }
                fundAccount.setBalance(fundAccount.getBalance() + totalAmount);
                status.setAmount(totalAmount);
            }
            // 处理冻结资金transaction.getFrozenAmount() > 0
            if (transaction.isFrozenTransaction()) {
                long availableAmount = fundAccount.getBalance() - fundAccount.getFrozenAmount();
                // 判断账户余额是否充足
                if (availableAmount - transaction.getFrozenAmount() < 0) {
                    throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户可用余额不足");
                }
                fundAccount.setFrozenAmount(fundAccount.getFrozenAmount() + transaction.getFrozenAmount());
            }

            fundAccount.setModifiedTime(transaction.getWhen());
            success = compareAndSetVersion(fundAccount);
            if (success) break;
        }

        if (!success) {
            throw new UserAccountException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        if (transaction.isFundTransaction()) {
            // 生成资金交易明细，sorted(FundActivity::compare)目的是保证先产生收入明细后产生支出明细，
            // 保证收支明细中期初余额不会为负数
            AtomicLong balance = new AtomicLong(status.getBalance());
            List<FundStatement> statements = Arrays.stream(transaction.getActivities())
                .filter(activity -> activity.getAmount() != 0).sorted(FundActivity::compare)
                .map(activity -> FundStatement.builder().paymentId(transaction.getPaymentId())
                    .accountId(account.getMasterAccountId()).childId(account.getChildAccountId())
                    .tradeType(transaction.getType()).action(ActionType.getByAmount(activity.getAmount()).getCode())
                    .balance(balance.getAndAdd(activity.getAmount())).amount(activity.getAmount())
                    .type(activity.getType()).typeName(activity.getTypeName()).description(activity.getDescription())
                    .createdTime(transaction.getWhen()).build())
                .collect(Collectors.toList());
            // 返回资金收支明细
            status.ofStreams(statements.stream().map(stmt -> TransactionStatus.FundStream.of(stmt.getBalance(), stmt.getAmount(),
                stmt.getType(), stmt.getTypeName(), stmt.getDescription())).collect(Collectors.toList()));
            fundStatementDao.insertFundStatements(DataPartition.strategy(fundAccount.getMchId()), statements);
        }
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * 注意：建议只在处理商户账户时使用此方法；
     * 使用数据库悲观锁防止资金数据并发修改，不需重试机制；
     * 1. 处理优先策略：资金解冻-资金交易（收入或支出）-资金冻结，此处理逻辑满足解冻并消费类业务（比如：预授权交易）
     * 或冻结并收入业务（先冻结后收入，冻结时异常提示余额不足），不会造成异常提示余额不足的情况
     * 2. 资金交易处理逻辑遵循收入优先，即收支明细先产生收入流水后产生支出流水；整个处理逻辑并不会影响余额校验，
     * 只是保证收支明细中期初余额不会出现负值（比如：余额为100，资金交易时先支出200后收入300）
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TransactionStatus submitExclusively(FundTransaction transaction) {
        CoreAccount account = transaction.getAccount();

        // 锁定资金账号避免数据并发修改
        FundAccount fundAccount = fundAccountService.lockFundAccountById(account.getMasterAccountId());
        TransactionStatus status = TransactionStatus.of(fundAccount.getAccountId(), fundAccount.getBalance(), 0L,
            fundAccount.getFrozenAmount(), transaction.getFrozenAmount(), transaction.getWhen());
        // 处理解冻资金transaction.getFrozenAmount()<0
        if (transaction.isUnfrozenTransaction()) {
            // 判断冻结余额是否充足
            if (fundAccount.getFrozenAmount() + transaction.getFrozenAmount() < 0) {
                throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户冻结余额不足");
            }
            fundAccount.setFrozenAmount(fundAccount.getFrozenAmount() + transaction.getFrozenAmount());
        }
        // 处理资金交易
        if (transaction.isFundTransaction()) {
            long totalAmount = Arrays.stream(transaction.getActivities()).mapToLong(FundActivity::getAmount).sum();
            long availableAmount = fundAccount.getBalance() - fundAccount.getFrozenAmount();
            // 如果为资金支出(totalAmount<0)则判断账户余额是否充足
            if (availableAmount + totalAmount < 0) {
                throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户可用余额不足");
            }
            fundAccount.setBalance(fundAccount.getBalance() + totalAmount);
            status.setAmount(totalAmount);
        }
        // 处理冻结资金transaction.getFrozenAmount() > 0
        if (transaction.isFrozenTransaction()) {
            long availableAmount = fundAccount.getBalance() - fundAccount.getFrozenAmount();
            // 判断账户余额是否充足
            if (availableAmount - transaction.getFrozenAmount() < 0) {
                throw new UserAccountException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户可用余额不足");
            }
            fundAccount.setFrozenAmount(fundAccount.getFrozenAmount() + transaction.getFrozenAmount());
        }
        fundAccount.setModifiedTime(transaction.getWhen());
        fundAccountDao.updateByAccountId(fundAccount);

        if (transaction.isFundTransaction()) {
            // 生成资金交易明细，sorted(FundActivity::compare)目的是保证先产生收入明细后产生支出明细，
            // 保证收支明细中期初余额不会为负数
            AtomicLong balance = new AtomicLong(status.getBalance());
            List<FundStatement> statements = Arrays.stream(transaction.getActivities())
                .filter(activity -> activity.getAmount() != 0).sorted(FundActivity::compare)
                .map(activity -> FundStatement.builder().paymentId(transaction.getPaymentId())
                    .accountId(account.getMasterAccountId()).childId(account.getChildAccountId())
                    .tradeType(transaction.getType()).action(ActionType.getByAmount(activity.getAmount()).getCode())
                    .balance(balance.getAndAdd(activity.getAmount())).amount(activity.getAmount())
                    .type(activity.getType()).typeName(activity.getTypeName()).description(activity.getDescription())
                    .createdTime(transaction.getWhen()).build())
                .collect(Collectors.toList());
            // 返回资金收支明细
            status.ofStreams(statements.stream().map(stmt -> TransactionStatus.FundStream.of(stmt.getBalance(), stmt.getAmount(),
                stmt.getType(), stmt.getTypeName(), stmt.getDescription())).collect(Collectors.toList()));
            fundStatementDao.insertFundStatements(DataPartition.strategy(fundAccount.getMchId()), statements);
        }
        return status;
    }

    private boolean compareAndSetVersion(FundAccount fundAccount) {
        return fundAccountDao.compareAndSetVersion(fundAccount) > 0;
    }
}
