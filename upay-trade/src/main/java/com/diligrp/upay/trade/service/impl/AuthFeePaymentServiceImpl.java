package com.diligrp.upay.trade.service.impl;

import com.diligrp.upay.core.domain.*;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IPreferenceService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.core.util.TransactionBuilder;
import com.diligrp.upay.pipeline.impl.AccountPipeline;
import com.diligrp.upay.pipeline.service.IAccountPipelineService;
import com.diligrp.upay.pipeline.type.ChannelType;
import com.diligrp.upay.sentinel.domain.Passport;
import com.diligrp.upay.sentinel.domain.RiskControlEngine;
import com.diligrp.upay.sentinel.service.IRiskControlService;
import com.diligrp.upay.sentinel.service.IUserPasswordService;
import com.diligrp.upay.sentinel.type.PassportType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.IFrozenOrderDao;
import com.diligrp.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.dao.ITradePaymentDao;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.FrozenOrder;
import com.diligrp.upay.trade.model.PaymentFee;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.model.TradePayment;
import com.diligrp.upay.trade.service.IPaymentComponentService;
import com.diligrp.upay.trade.type.*;
import com.diligrp.upay.trade.util.AccountStateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("authFeePaymentService")
public class AuthFeePaymentServiceImpl extends FeePaymentServiceImpl implements IPaymentComponentService {

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IPreferenceService preferenceService;

    @Resource
    private IUserPasswordService userPasswordService;

    @Resource
    private IAccountPipelineService accountPipelineService;

    @Resource
    private IRiskControlService riskControlService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult commit(Trade trade, Payment payment) {
        if (!ChannelType.forPreAuthFee(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行预授权缴费业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }

        ApplicationPermit application = trade.getApplication();
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = payment.getObject(DataPartition.class);
        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(account);
        Preference preference = preferenceService.getPreferences(merchant.getMchId());
        userPasswordService.checkUserPassword(account, payment.getPassword(), preference.getMaxPasswordErrors());

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = keyGenerator.nextId();
        keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();

        // 冻结资金
        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
        transaction.freeze(payment.getAmount());
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        // 创建冻结资金订单
        CoreAccount core = new CoreAccount(account.getAccountId(), account.getParentId());
        FrozenOrder frozenOrder = FrozenOrder.builder().paymentId(paymentId).accountId(core.getMasterAccountId())
            .childId(core.getChildAccountId()).name(account.getName()).type(FrozenType.TRADE_FROZEN.getCode())
            .amount(trade.getAmount()).state(FrozenState.FROZEN.getCode()).description(null).version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);

        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(supportType().getCode()).outTradeNo(trade.getOutTradeNo())
            .accountId(account.getAccountId()).name(account.getName()).amount(trade.getAmount())
            .maxAmount(trade.getAmount()).fee(0L).goods(trade.getGoods()).state(TradeState.FROZEN.getCode())
            .description(trade.getDescription()).version(0).createdTime(now).modifiedTime(now).build();
        tradeOrderDao.insertTradeOrder(partition, tradeOrder);
        TradePayment tradePayment = TradePayment.builder().paymentId(paymentId).tradeId(tradeId)
            .channelId(payment.getChannelId()).payType(payment.getPayType()).accountId(account.getAccountId())
            .name(account.getName()).amount(payment.getAmount()).fee(0L).protocolId(null).cycleNo(payment.getCycleNo())
            .state(PaymentState.PROCESSING.getCode()).description(null).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);

        return PaymentResult.of(tradeId, PaymentState.SUCCESS, "预授权资金冻结成功", status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult confirm(TradeOrder trade, Confirm confirm) {
        List<Fee> fees = confirm.getObjects(Fee.class).orElse(Collections.emptyList());
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != confirm.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际缴费金额与申请缴费金额不一致");
        }

        DataPartition partition = confirm.getObject(DataPartition.class);
        MerchantPermit merchant = confirm.getObject(MerchantPermit.class);
        Optional<TradePayment> paymentOpt = tradePaymentDao.findByTradeId(partition, trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!payment.getAccountId().equals(confirm.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }

        // 查询冻结订单
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findByPaymentId(payment.getPaymentId());
        FrozenOrder frozenOrder = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (frozenOrder.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无预授权资金冻结记录");
        }

        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(account);
        Preference preference = preferenceService.getPreferences(merchant.getMchId());
        userPasswordService.checkUserPassword(account, confirm.getPassword(), preference.getMaxPasswordErrors());
        RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(account);
        Passport passport = Passport.of(PassportType.FOR_TRADE, account.getAccountId(), payment.getAmount());
        riskControlEngine.checkPassport(passport);

        // 客户账号资金解冻并缴费
        LocalDateTime now = LocalDateTime.now().withNano(0);
        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(payment.getPaymentId(), trade.getType(), now);
        transaction.unfreeze(frozenOrder.getAmount());
        fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        // 修改冻结订单"已解冻"状态
        FrozenStateDTO frozenState = FrozenStateDTO.of(frozenOrder.getPaymentId(), FrozenState.UNFROZEN.getCode(),
            frozenOrder.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        // "预授权缴费"的交易单中金额修改成"实际缴费金额"，交易订单中max_amount金额为冻结金额
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), totalFee, TradeState.SUCCESS.getCode(),
            trade.getVersion(), now);
        tradeState.setMaxAmount(totalFee);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        // "预授权缴费"的支付单中金额修改成"实际缴费金额"，并存储费用明细
        PaymentStateDTO paymentState = PaymentStateDTO.of(payment.getPaymentId(), totalFee,
            PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
        if (tradePaymentDao.compareAndSetState(partition, paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        List<PaymentFee> paymentFees = fees.stream().map(fee ->
            PaymentFee.of(payment.getPaymentId(), fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFees);

        AccountPipeline profitPipeline = AccountPipeline.of(merchant.getProfitAccount());
        TransactionBuilder profit = profitPipeline.openTransaction(payment.getPaymentId(), trade.getType(), now);
        fees.forEach(fee -> profit.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        accountPipelineService.submitExclusively(profit::build);

        riskControlEngine.admitPassport(passport);
        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "预授权确认成功", status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        // 预授权缴费成功则正常撤销"缴费"交易
        if (trade.getState() == TradeState.SUCCESS.getCode()) {
            return super.cancel(trade, cancel);
        }

        DataPartition partition = cancel.getObject(DataPartition.class);
        MerchantPermit merchant = cancel.getObject(MerchantPermit.class);
        Optional<TradePayment> paymentOpt = tradePaymentDao.findByTradeId(partition, trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(account);

        // 撤销预授权，需验证缴费账户状态无须验证密码
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findByPaymentId(payment.getPaymentId());
        FrozenOrder order = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无预授权资金冻结记录");
        }

        // 客户账号资金解冻
        LocalDateTime now = LocalDateTime.now().withNano(0);
        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(payment.getPaymentId(), trade.getType(), now);
        transaction.unfreeze(order.getAmount());
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        // 修改冻结订单状态
        FrozenStateDTO frozenState = FrozenStateDTO.of(order.getPaymentId(), FrozenState.UNFROZEN.getCode(),
            order.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        // 撤销支付记录
        PaymentStateDTO paymentState = PaymentStateDTO.of(payment.getPaymentId(), PaymentState.FAILED.getCode(),
            payment.getVersion(), now);
        if (tradePaymentDao.compareAndSetState(partition, paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        // 撤销交易订单
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), TradeState.CLOSED.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "撤销预授权缴费成功", status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.AUTH_FEE;
    }
}
