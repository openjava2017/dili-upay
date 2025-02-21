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
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.IRefundPaymentDao;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.dao.ITradePaymentDao;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
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

@Service("depositPaymentService")
public class DepositPaymentServiceImpl implements IPaymentComponentService {

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private ITradePaymentDao tradePaymentDao;

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
        if (!ChannelType.forDeposit(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行充值业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "充值资金账号不一致");
        }

        // 验证账户密码和风控设置
        ApplicationPermit application = trade.getApplication();
        MerchantPermit merchant = application.getMerchant();
        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        AccountStateMachine.checkAccountTradeState(account);
        Preference preference = preferenceService.getPreferences(merchant.getMchId());
        userPasswordService.checkUserPassword(account, payment.getPassword(), preference.getMaxPasswordErrors());
        RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(account);
        Passport passport = Passport.of(PassportType.FOR_DEPOSIT, account.getAccountId(), payment.getAmount());
        riskControlEngine.checkPassport(passport);

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = keyGenerator.nextId();
        keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();
        DataPartition partition = payment.getObject(DataPartition.class);

        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, supportType().getCode(), now);
        transaction.income(payment.getAmount(), 0, "充值金额", "账户充值");
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(supportType().getCode()).outTradeNo(trade.getOutTradeNo())
            .accountId(account.getAccountId()).name(account.getName()).amount(trade.getAmount())
            .maxAmount(trade.getAmount()).fee(0L).goods(trade.getGoods()).state(TradeState.SUCCESS.getCode())
            .description(trade.getDescription()).version(0).createdTime(now).modifiedTime(now).build();
        tradeOrderDao.insertTradeOrder(partition, tradeOrder);
        TradePayment tradePayment = TradePayment.builder().paymentId(paymentId).tradeId(tradeId)
            .channelId(payment.getChannelId()).payType(payment.getPayType()).accountId(account.getAccountId())
            .name(account.getName()).amount(payment.getAmount()).fee(0L).protocolId(null).cycleNo(payment.getCycleNo())
            .state(PaymentState.SUCCESS.getCode()).description(null).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);

        riskControlEngine.admitPassport(passport);
        return PaymentResult.of(tradeId, PaymentState.SUCCESS, "充值成功", status);
    }

    /**
     * 充值冲正用于处理系统充值金额大于实际充值金额的情况，冲正时账户退出多余充值金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResult correct(TradeOrder trade, Correct correct) {
        AssertUtils.isTrue(ObjectUtils.equals(trade.getAccountId(), correct.getAccountId()), "冲正资金账号不一致");
        AssertUtils.isTrue(correct.getAmount() < 0, "充值冲正金额非法");
        AssertUtils.isTrue(correct.getAmount() + trade.getAmount() >= 0, "冲正金额不能大于原操作金额");
        DataPartition partition = correct.getObject(DataPartition.class);
        TradePayment payment = tradePaymentDao.findByTradeId(partition, trade.getTradeId())
            .orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        MerchantPermit merchant = correct.getObject(MerchantPermit.class);
        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), payment.getAccountId());
        // 处理原账户的冲正, 账户出账金额 = ABS(冲正金额(负数)-冲正费用(负数))
        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextId();

        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(paymentId, TradeType.REFUND_TRADE.getCode(), now);
        transaction.outgo(Math.abs(correct.getAmount()), 0, "充值金额", "充值冲正");
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        RefundPayment refundPayment = RefundPayment.builder().paymentId(paymentId).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(payment.getChannelId())
            .amount(correct.getAmount()).fee(0L).cycleNo(correct.getCycleNo()).state(PaymentState.SUCCESS.getCode())
            .version(0).createdTime(now).modifiedTime(now).build();
        refundPaymentDao.insertRefundPayment(refundPayment);

        // 更正交易订单
        Long newAmount = trade.getAmount() + correct.getAmount();
        TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        return PaymentResult.of(trade.getTradeId(), PaymentState.SUCCESS, "充值冲正成功", status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.DEPOSIT;
    }
}
