package com.diligrp.upay.trade.service.impl;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.pipeline.type.ChannelType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.Constants;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.service.IPaymentComponentService;
import com.diligrp.upay.trade.service.IPaymentPlatformService;
import com.diligrp.upay.trade.type.TradeState;
import com.diligrp.upay.trade.type.TradeType;
import jakarta.annotation.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service("paymentPlatformService")
public class PaymentPlatformServiceImpl implements IPaymentPlatformService, BeanPostProcessor {

    private final Map<TradeType, IPaymentComponentService> services = new HashMap<>();

    @Lazy // BeanPostProcessor中的依赖需标记为Lazy
    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Lazy
    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 提交交易支付：适用于所有交易，不同的交易类型有不同的业务处理逻辑
     */
    @Override
    public PaymentResult commit(ApplicationPermit application, TradeDTO request) {
        PaymentDTO paymentDTO = request.getPayment();
        ChannelType.getType(paymentDTO.getChannelId()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的支付渠道"));
        TradeType tradeType = TradeType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));
        IPaymentComponentService service = paymentService(tradeType).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));

        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        if (tradeOrderDao.findByOutTradeNo(partition, merchant.getMchId(), request.getOutTradeNo()).isPresent()) {
            throw new TradePaymentException(ErrorCode.OBJECT_ALREADY_EXISTS, "Duplicate outTradeNo");
        }
        Trade trade = Trade.of(request.getAccountId(), request.getAmount(), request.getOutTradeNo(),
            request.getGoods(), request.getDescription());
        trade.attach(application);

        Payment payment = Payment.of(paymentDTO.getAccountId(), paymentDTO.getChannelId(),
            paymentDTO.getPayType(), trade.getAmount(), paymentDTO.getPassword(), paymentDTO.getCycleNo());
        payment.attach(partition).attach(merchant).attach(Constants.PROTOCOL_ID, paymentDTO.getProtocolId());
        paymentDTO.fees().ifPresent(fees -> payment.put(Fee.class.getName(), fees));
        paymentDTO.channel().ifPresent(channel -> payment.put(Channel.class.getName(), channel));
        return service.commit(trade, payment);
    }

    /**
     * 确认预授权消费：适用于预授权业务（预授权缴费和预授权交易）
     */
    @Override
    public PaymentResult confirm(ApplicationPermit application, ConfirmDTO request) {
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findByTradeId(partition, request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forConfirm(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "无效的交易状态，不能确认消费");
        }

        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));
        IPaymentComponentService service = paymentService(tradeType).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));

        Confirm confirm = Confirm.of(request.getAccountId(), request.getAmount(), request.getPassword());
        confirm.attach(partition).attach(merchant);
        request.fees().ifPresent(fees -> confirm.put(Fee.class.getName(), fees));
        return service.confirm(trade, confirm);
    }

    /**
     * 交易退款：支持部分退款或全额退款
     */
    @Override
    public PaymentResult refund(ApplicationPermit application, RefundDTO request) {
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findByTradeId(partition, request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forRefund(trade.getState())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行交易退款");
        }

        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));
        IPaymentComponentService service = paymentService(tradeType).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));

        Refund refund = Refund.of(trade.getTradeId(), request.getAmount(), request.getCycleNo());
        refund.attach(partition).attach(merchant);
        request.fees().ifPresent(fees -> refund.put(Fee.class.getName(), fees));
        return service.refund(trade, refund);
    }

    /**
     * 撤销交易：撤销预授权业务时将解冻冻结资金，撤销普通业务时将进行资金逆向操作
     */
    @Override
    public PaymentResult cancel(ApplicationPermit application, RefundDTO request) {
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findByTradeId(partition, request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forCancel(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "无效的交易状态，不能撤销交易");
        }

        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));
        IPaymentComponentService service = paymentService(tradeType).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));

        Refund cancel = Refund.of(trade.getTradeId(), trade.getAmount(), request.getCycleNo());
        cancel.attach(partition).attach(merchant);
        return service.cancel(trade, cancel);
    }

    /**
     * 交易冲正：目前只有充值、提现允许进行交易冲正
     */
    @Override
    public PaymentResult correct(ApplicationPermit application, CorrectDTO request) {
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findByTradeId(partition, request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forCorrect(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "无效的交易状态，不能进行交易冲正");
        }

        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));
        IPaymentComponentService service = paymentService(tradeType).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不支持的交易类型"));

        Correct correct = Correct.of(request.getTradeId(), request.getAccountId(), request.getAmount(), request.getCycleNo());
        correct.attach(partition).attach(merchant);
        return service.correct(trade, correct);
    }

    @NonNull
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @NonNull
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof IPaymentComponentService paymentService) {
            services.put(paymentService.supportType(), paymentService);
        }
        return bean;
    }

    /**
     * 检查商户是否有权限操作该交易订单: 接口权限商户与交易订单所属商户必须有共同的父商户
     */
    private void checkTradePermission(TradeOrder order, MerchantPermit permit) {
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(order.getMchId());
        if (!ObjectUtils.equals(merchant.parentMchId(), permit.parentMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "商户没有权限操作此交易订单");
        }
    }

    private Optional<IPaymentComponentService> paymentService(TradeType tradeType) {
        return Optional.ofNullable(services.get(tradeType));
    }
}
