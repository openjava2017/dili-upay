package com.diligrp.upay.trade.service.impl;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.domain.Preference;
import com.diligrp.upay.core.domain.TransactionStatus;
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
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.upay.trade.dao.IRefundPaymentDao;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.dao.ITradePaymentDao;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.PaymentFee;
import com.diligrp.upay.trade.model.RefundPayment;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.model.TradePayment;
import com.diligrp.upay.trade.service.IPaymentComponentService;
import com.diligrp.upay.trade.type.FeeUseFor;
import com.diligrp.upay.trade.type.PaymentState;
import com.diligrp.upay.trade.type.TradeState;
import com.diligrp.upay.trade.type.TradeType;
import com.diligrp.upay.trade.util.AccountStateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("tradePaymentService")
public class TradePaymentServiceImpl implements IPaymentComponentService {

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

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
        if (!ChannelType.forTrade(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行即时交易业务");
        }
        if (trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "同一账号不能进行交易");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElse(Collections.emptyList());
        fees.forEach(FeeUseFor::checkUseFor);

        MerchantPermit merchant = payment.getObject(MerchantPermit.class);
        UserAccount fromAccount = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(fromAccount);
        UserAccount toAccount = userAccountService.findUserAccountById(merchant.parentMchId(), trade.getAccountId());
        AccountStateMachine.checkAccountTradeState(toAccount);
        if (!ObjectUtils.equals(fromAccount.getMchId(), toAccount.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户交易");
        }

        Preference preference = preferenceService.getPreferences(merchant.getMchId());
        userPasswordService.checkUserPassword(fromAccount, payment.getPassword(), preference.getMaxPasswordErrors());
        RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(fromAccount);
        Passport passport = Passport.ofTrade(fromAccount.getAccountId(), payment.getAmount());
        riskControlEngine.checkPassport(passport);

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = keyGenerator.nextId();
        keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        ApplicationPermit application = trade.getApplication();
        DataPartition partition = payment.getObject(DataPartition.class);

        List<TransactionStatus> statusList = new ArrayList<>(2);
        AccountPipeline fromPipeline = AccountPipeline.of(fromAccount);
        TransactionBuilder fromTransaction = fromPipeline.openTransaction(paymentId, supportType().getCode(), now);
        fromTransaction.outgo(trade.getAmount(), 0, "交易资金", "及时交易");
        fees.stream().filter(FeeUseFor::forBuyer).forEach(fee ->
            fromTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        statusList.add(accountPipelineService.submit(fromTransaction::build));
        AccountPipeline toPipeline = AccountPipeline.of(toAccount);
        TransactionBuilder toTransaction = toPipeline.openTransaction(paymentId, supportType().getCode(), now);
        toTransaction.income(trade.getAmount(), 0, "交易资金", "及时交易");
        fees.stream().filter(FeeUseFor::forSeller).forEach(fee ->
            toTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        statusList.add(accountPipelineService.submit(toTransaction::build));

        long fromFee = fees.stream().filter(FeeUseFor::forBuyer).mapToLong(Fee::getAmount).sum();
        long toFee = fees.stream().filter(FeeUseFor::forSeller).mapToLong(Fee::getAmount).sum();
        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(supportType().getCode()).outTradeNo(trade.getOutTradeNo())
            .accountId(toAccount.getAccountId()).name(toAccount.getName()).amount(trade.getAmount())
            .maxAmount(trade.getAmount()).fee(toFee).goods(trade.getGoods()).state(TradeState.SUCCESS.getCode())
            .description(trade.getDescription()).version(0).createdTime(now).modifiedTime(now).build();
        tradeOrderDao.insertTradeOrder(partition, tradeOrder);
        TradePayment tradePayment = TradePayment.builder().paymentId(paymentId).tradeId(tradeId)
            .channelId(payment.getChannelId()).payType(payment.getPayType()).accountId(fromAccount.getAccountId())
            .name(fromAccount.getName()).amount(payment.getAmount()).fee(fromFee).protocolId(null).cycleNo(payment.getCycleNo())
            .state(PaymentState.SUCCESS.getCode()).description(null).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getUseFor(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFeeDos);

        if (!fees.isEmpty()) {
            AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
            TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            accountPipelineService.submitExclusively(transaction::build);
        }

        riskControlEngine.admitPassport(passport);
        return PaymentResult.of(tradeId, PaymentState.SUCCESS, "交易成功", statusList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        DataPartition partition = cancel.getObject(DataPartition.class);
        MerchantPermit merchant = cancel.getObject(MerchantPermit.class);
        Optional<TradePayment> paymentOpt = tradePaymentDao.findByTradeId(partition, trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(partition, payment.getPaymentId());

        UserAccount fromAccount = userAccountService.findUserAccountById(trade.getAccountId());
        AccountStateMachine.checkAccountTradeState(fromAccount);
        UserAccount toAccount = userAccountService.findUserAccountById(payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(toAccount);

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();

        List<TransactionStatus> statusList = new ArrayList<>(2);
        AccountPipeline fromPipeline = AccountPipeline.of(fromAccount);
        TransactionBuilder fromTransaction = fromPipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
        fromTransaction.outgo(cancel.getAmount(), 0, "交易资金", "撤销交易");
        fees.stream().filter(FeeUseFor::forSeller).forEach(fee ->
            fromTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        statusList.add(accountPipelineService.submit(fromTransaction::build));
        AccountPipeline toPipeline = AccountPipeline.of(toAccount);
        TransactionBuilder toTransaction = toPipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
        toTransaction.income(cancel.getAmount(), 0, "交易资金", "撤销交易");
        fees.stream().filter(FeeUseFor::forBuyer).forEach(fee ->
            toTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        statusList.add(accountPipelineService.submit(toTransaction::build));

        RefundPayment refundPayment = RefundPayment.builder().paymentId(paymentId).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(payment.getChannelId())
            .amount(cancel.getAmount()).fee(0L).cycleNo(cancel.getCycleNo()).state(PaymentState.SUCCESS.getCode())
            .version(0).createdTime(now).modifiedTime(now).build();
        refundPaymentDao.insertRefundPayment(refundPayment);
        // 撤销交易订单
        Long newAmount = trade.getAmount() - cancel.getAmount();
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        if (!fees.isEmpty()) {
            AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
            TransactionBuilder transaction = pipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
            fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            accountPipelineService.submitExclusively(transaction::build);
        }

        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "交易退款成功", statusList);
    }

    @Override
    public TradeType supportType() {
        return TradeType.DIRECT_TRADE;
    }
}
