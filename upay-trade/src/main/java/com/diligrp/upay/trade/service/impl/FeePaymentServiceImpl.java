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
import com.diligrp.upay.sentinel.type.PassportType;
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
import com.diligrp.upay.trade.type.PaymentState;
import com.diligrp.upay.trade.type.TradeState;
import com.diligrp.upay.trade.type.TradeType;
import com.diligrp.upay.trade.util.AccountStateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("feePaymentService")
public class FeePaymentServiceImpl implements IPaymentComponentService {

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
        if (!ChannelType.forFee(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行缴费业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }

        List<Fee> fees = payment.getObjects(Fee.class).orElse(Collections.emptyList());
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际缴费金额与申请缴费金额不一致");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = keyGenerator.nextId();
        keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        ApplicationPermit application = trade.getApplication();
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = payment.getObject(DataPartition.class);
        TransactionStatus status = null;
        UserAccount account = null;

        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
            AccountStateMachine.checkAccountTradeState(account);

            Preference preference = preferenceService.getPreferences(merchant.getMchId());
            userPasswordService.checkUserPassword(account, payment.getPassword(), preference.getMaxPasswordErrors());
            RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(account);
            Passport passport = Passport.of(PassportType.FOR_TRADE, account.getAccountId(), payment.getAmount());
            riskControlEngine.checkPassport(passport);

            AccountPipeline pipeline = AccountPipeline.of(account);
            TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
            fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountPipelineService.submit(transaction::build);
            riskControlEngine.admitPassport(passport);
        }

        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(supportType().getCode()).outTradeNo(trade.getOutTradeNo())
            .accountId(account != null ? account.getAccountId() : payment.getAccountId())
            .name(account != null ? account.getName() : "匿名用户").amount(trade.getAmount())
            .maxAmount(trade.getAmount()).fee(0L).goods(trade.getGoods()).state(TradeState.SUCCESS.getCode())
            .description(trade.getDescription()).version(0).createdTime(now).modifiedTime(now).build();
        tradeOrderDao.insertTradeOrder(partition, tradeOrder);
        TradePayment tradePayment = TradePayment.builder().paymentId(paymentId).tradeId(tradeId)
            .channelId(payment.getChannelId()).payType(payment.getPayType())
            .accountId(account != null ? account.getAccountId() : payment.getAccountId())
            .name(account != null ? account.getName() : "匿名用户").amount(payment.getAmount())
            .fee(0L).protocolId(null).cycleNo(payment.getCycleNo()).state(PaymentState.SUCCESS.getCode())
            .description(null).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFeeDos);

        AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
        fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        accountPipelineService.submitExclusively(transaction::build);
        return PaymentResult.of(tradeId, PaymentState.SUCCESS, "缴费成功", status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult refund(TradeOrder trade, Refund refund) {
        List<Fee> fees = refund.getObjects(Fee.class).orElse(Collections.emptyList());
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != refund.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际退费金额与申请退费金额不一致");
        }
        if (trade.getAmount() < totalFee) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "申请退费金额超过原支付金额");
        }

        MerchantPermit merchant = refund.getObject(MerchantPermit.class);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        TransactionStatus status = null;

        DataPartition partition = refund.getObject(DataPartition.class);
        Optional<TradePayment> paymentOpt = tradePaymentDao.findByTradeId(partition, trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
            AccountPipeline pipeline = AccountPipeline.of(account);
            TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountPipelineService.submit(transaction::build);
        }

        RefundPayment refundPayment = RefundPayment.builder().paymentId(paymentId).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(payment.getChannelId())
            .amount(refund.getAmount()).fee(0L).cycleNo(refund.getCycleNo()).state(PaymentState.SUCCESS.getCode())
            .version(0).createdTime(now).modifiedTime(now).build();
        refundPaymentDao.insertRefundPayment(refundPayment);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFeeDos);
        // 更正交易订单
        Long newAmount = trade.getAmount() - refund.getAmount();
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
        fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        accountPipelineService.submitExclusively(transaction::build);

        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "缴费退款成功", status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        DataPartition partition = cancel.getObject(DataPartition.class);
        Optional<TradePayment> paymentOpt = tradePaymentDao.findByTradeId(partition, trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(partition, payment.getPaymentId());
        MerchantPermit merchant = cancel.getObject(MerchantPermit.class);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        TransactionStatus status = null;

        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
            AccountPipeline pipeline = AccountPipeline.of(account);
            TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountPipelineService.submit(transaction::build);
        }

        RefundPayment refundPayment = RefundPayment.builder().paymentId(paymentId).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(payment.getChannelId())
            .amount(cancel.getAmount()).fee(0L).cycleNo(cancel.getCycleNo()).state(PaymentState.SUCCESS.getCode())
            .version(0).createdTime(now).modifiedTime(now).build();
        refundPaymentDao.insertRefundPayment(refundPayment);
        // 更正交易订单
        Long newAmount = trade.getAmount() - cancel.getAmount();
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
        fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        accountPipelineService.submitExclusively(transaction::build);

        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "撤销缴费成功", status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.PAY_FEE;
    }
}
