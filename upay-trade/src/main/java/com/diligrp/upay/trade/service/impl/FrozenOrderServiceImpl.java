package com.diligrp.upay.trade.service.impl;

import com.diligrp.upay.core.dao.IUserAccountDao;
import com.diligrp.upay.core.domain.TransactionStatus;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.core.util.TransactionBuilder;
import com.diligrp.upay.pipeline.impl.AccountPipeline;
import com.diligrp.upay.pipeline.service.IAccountPipelineService;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.PageMessage;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.KeyGeneratorManager;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.trade.dao.IFrozenOrderDao;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.FrozenOrder;
import com.diligrp.upay.trade.service.IFrozenOrderService;
import com.diligrp.upay.trade.type.FrozenState;
import com.diligrp.upay.trade.type.FrozenType;
import com.diligrp.upay.trade.util.AccountStateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 资金冻结/解冻订单服务实现
 */
@Service("frozenOrderService")
public class FrozenOrderServiceImpl implements IFrozenOrderService {

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IAccountPipelineService accountPipelineService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     *  人工冻结资金
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FrozenStatus freeze(FreezeFundDTO request) {
        Optional<FrozenType> frozenTypeOpt = FrozenType.getType(request.getType());
        frozenTypeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持此冻结类型"));
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(request.getAccountId());
        UserAccount account = accountOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::checkFrozenFund);

        // 冻结资金
        LocalDateTime now = LocalDateTime.now();
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, FrozenState.FROZEN.getCode(), now);
        transaction.freeze(request.getAmount());
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        // 创建冻结资金订单
        FrozenOrder frozenOrder = FrozenOrder.builder().paymentId(paymentId).accountId(request.getAccountId())
            .name(account.getName()).type(request.getType()).amount(request.getAmount())
            .extension(request.getExtension()).state(FrozenState.FROZEN.getCode()).description(request.getDescription())
            .version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);
        return FrozenStatus.of(paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     *  人工解冻资金
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FrozenStatus unfreeze(String paymentId) {
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findByPaymentId(paymentId);
        FrozenOrder order = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效冻结状态，不能执行解冻操作");
        }
        if (order.getType() == FrozenType.TRADE_FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能解冻交易冻结的资金");
        }
        Optional<UserAccount> accountOpt = userAccountDao.findByAccountId(order.getAccountId());
        UserAccount account = accountOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::checkFrozenFund);

        LocalDateTime now = LocalDateTime.now();
        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, FrozenState.UNFROZEN.getCode(), now);
        transaction.unfreeze(order.getAmount());
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        FrozenStateDTO updateState = FrozenStateDTO.of(paymentId, FrozenState.UNFROZEN.getCode(), order.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(updateState) <= 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        return FrozenStatus.of(paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     *  根据主资金账号进行分页查询冻结资金订单，当传入子账号进行查询时，将返回空结果
     */
    @Override
    public PageMessage<FrozenOrder> listFrozenOrders(FrozenOrderQuery query) {
        // 检查资金账号是否存在
        userAccountService.findUserAccountById(query.getAccountId());
        List<FrozenOrder> frozenOrders = Collections.emptyList();
        long total = frozenOrderDao.countFrozenOrders(query);
        if (total > 0) {
            frozenOrders = frozenOrderDao.listFrozenOrders(query);
        }
        return PageMessage.success(total, frozenOrders);
    }

    /**
     * {@inheritDoc}
     *
     * 参数传入主资金账号ID
     */
    @Override
    public Optional<FrozenAmount> findFrozenAmount(Long accountId) {
        return frozenOrderDao.findFrozenAmount(accountId);
    }
}
