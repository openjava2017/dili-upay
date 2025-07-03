package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.core.util.TransactionBuilder;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.pipeline.dao.IWechatPaymentDao;
import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.WechatConfig;
import com.diligrp.upay.pipeline.domain.wechat.WechatMerchant;
import com.diligrp.upay.pipeline.domain.wechat.WechatPaymentDTO;
import com.diligrp.upay.pipeline.impl.AccountPipeline;
import com.diligrp.upay.pipeline.impl.WechatPartnerPipeline;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.pipeline.service.IAccountPipelineService;
import com.diligrp.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.upay.pipeline.type.ChannelType;
import com.diligrp.upay.pipeline.type.WechatPaymentType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.type.SnowflakeKey;
import com.diligrp.upay.shared.uid.KeyGenerator;
import com.diligrp.upay.shared.uid.SnowflakeKeyManager;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.upay.trade.dao.IRefundPaymentDao;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.dao.ITradePaymentDao;
import com.diligrp.upay.trade.domain.Fee;
import com.diligrp.upay.trade.domain.TradeStateDTO;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.message.MessageQueueService;
import com.diligrp.upay.trade.message.TaskMessage;
import com.diligrp.upay.trade.model.PaymentFee;
import com.diligrp.upay.trade.model.RefundPayment;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.model.TradePayment;
import com.diligrp.upay.trade.service.IWechatFeeService;
import com.diligrp.upay.trade.type.PaymentState;
import com.diligrp.upay.trade.type.TradeState;
import com.diligrp.upay.trade.type.TradeType;
import com.diligrp.upay.trade.util.WechatStateUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("wechatFeeService")
public class WechatFeeServiceImpl implements IWechatFeeService {

    private static final Logger LOG = LoggerFactory.getLogger(WechatFeeServiceImpl.class);

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private IWechatPaymentDao wechatPaymentDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IAccountPipelineService accountPipelineService;

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    @Resource
    private MessageQueueService messageQueueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request) {
        WechatPaymentType paymentType = WechatPaymentType.getType(request.getPayType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的微信支付方式"));
        if (paymentType == WechatPaymentType.JSAPI) {
            AssertUtils.notEmpty(request.getOpenId(), "openId missed");
        }
        List<Fee> fees = request.fees().orElse(Collections.emptyList());
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != request.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际缴费金额与申请缴费金额不一致");
        }

        MerchantPermit merchant = application.getMerchant();
        WechatPipeline pipeline = paymentPipelineManager.findPipelineByMchId(merchant.getMchId(), WechatPipeline.class);
        if (pipeline instanceof WechatPartnerPipeline) {
            AssertUtils.notEmpty(request.getMchId(), "参数错误: 未提供子商户信息");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator paymentIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = String.valueOf(paymentIdKey.nextId());
        WechatPrepayRequest prepayRequest = WechatPrepayRequest.of(paymentId, request.getOpenId(), request.getAmount(),
            request.getGoods(), request.getDescription(), now);
        prepayRequest.attach(WechatMerchant.of(request.getMchId(), request.getAppId()));
        WechatPrepayResponse prepayResponse = switch (paymentType) {
            case JSAPI -> pipeline.sendJsApiPrepayRequest(prepayRequest);
            case NATIVE -> pipeline.sendNativePrepayRequest(prepayRequest);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "缴费业务不支持此类微信支付方式");
        };

        KeyGenerator tradeIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = String.valueOf(tradeIdKey.nextId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(TradeType.ONLINE_FEE.getCode()).outTradeNo(request.getOutTradeNo())
            .accountId(request.getAccountId()).name("微信用户").amount(request.getAmount())
            .maxAmount(request.getAmount()).fee(0L).goods(request.getGoods()).state(TradeState.PENDING.getCode())
            .description(request.getDescription()).version(0).createdTime(now).modifiedTime(now).build();
        tradeOrderDao.insertTradeOrder(partition, tradeOrder);

        WechatConfig config = pipeline.getClient().getWechatConfig();
        String wxMchId = pipeline instanceof WechatPartnerPipeline ? request.getMchId() : config.getMchId();
        String appId = pipeline instanceof WechatPartnerPipeline ? request.getAppId() : config.getAppId();
        String objectId = switch (paymentType) {
            case JSAPI -> ((JsApiPrepayResponse)prepayResponse).getPrepayId();
            case NATIVE -> ((NativePrepayResponse)prepayResponse).getCodeUrl();
            default -> null;
        };
        WechatPayment payment = WechatPayment.builder().mchId(merchant.getMchId()).wxMchId(wxMchId).appId(appId)
            .tradeId(tradeId).type(TradeType.ONLINE_FEE.getCode()).paymentId(paymentId).payType(paymentType.getCode())
            .pipelineId(pipeline.pipelineId()).accountId(request.getAccountId()).name("微信用户")
            .goods(request.getGoods()).amount(request.getAmount()).objectId(objectId).openId(null)
            .payTime(null).outTradeNo(null).state(PaymentState.PENDING.getCode()).notifyUri(request.getNotifyUri())
            .description(null).version(0).createdTime(now).modifiedTime(now).build();
        wechatPaymentDao.insertWechatPayment(payment);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFeeDos);

        // 发送MQ延时消息: 实现十分钟后根据微信支付查询结果，关闭或完成本地支付订单
        messageQueueService.sendWechatScanMessage(TaskMessage.of(TaskMessage.TYPE_WECHAT_PREPAY_SCAN, paymentId));
        return prepayResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyPaymentResult(WechatPaymentResponse response) {
        WechatPayment payment = response.getObject(WechatPayment.class);
        PaymentState paymentState = WechatStateUtils.getPaymentState(response.getState());
        if (paymentState != PaymentState.SUCCESS && paymentState != PaymentState.FAILED) {
            LOG.warn("Ignore wechat order payment notification for {}:{}", payment.getPaymentId(), response.getState());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(payment.getMchId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder trade = tradeOrderDao.findByTradeId(partition, payment.getTradeId()).get();
        WechatPaymentDTO paymentDTO = WechatPaymentDTO.builder().paymentId(payment.getPaymentId())
            .outTradeNo(response.getOutTradeNo()).openId(response.getOpenId()).payTime(response.getWhen())
            .state(paymentState.getCode()).description(response.getMessage()).version(payment.getVersion())
            .modifiedTime(now).build();
        if (wechatPaymentDao.compareAndSetState(paymentDTO) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        TradePayment tradePayment = TradePayment.builder().paymentId(payment.getPaymentId()).tradeId(trade.getTradeId())
            .channelId(ChannelType.WXPAY.getCode()).payType(payment.getPayType()).accountId(payment.getAccountId())
            .name(payment.getName()).amount(payment.getAmount()).fee(0L).protocolId(null).cycleNo(null)
            .state(paymentState.getCode()).description(response.getMessage()).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);
        TradeState tradeState = paymentState == PaymentState.SUCCESS ? TradeState.SUCCESS : TradeState.FAILED;
        TradeStateDTO tradeStateDTO = TradeStateDTO.of(trade.getTradeId(), tradeState.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeStateDTO) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        if (paymentState == PaymentState.SUCCESS) {
            List<PaymentFee> fees = paymentFeeDao.findPaymentFees(partition, payment.getPaymentId());
            AccountPipeline pipeline = AccountPipeline.of(merchant.getProfitAccount());
            TransactionBuilder transaction = pipeline.openTransaction(payment.getPaymentId(), TradeType.ONLINE_FEE.getCode(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            accountPipelineService.submitExclusively(transaction::build);
        }
        WechatPaymentResult paymentResult = new WechatPaymentResult(payment.getPaymentId(), paymentState.getCode(),
            trade.getOutTradeNo(), now, response.getMessage());
        messageQueueService.sendWechatNotifyMessage(payment.getNotifyUri(), paymentResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePrepayOrder(WechatPayment payment) {
        if (!PaymentState.PENDING.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "不能关闭微信订单: 无效的订单状态");
        }
        LocalDateTime now = LocalDateTime.now();
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(payment.getMchId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder trade = tradeOrderDao.findByTradeId(partition, payment.getTradeId()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付订单不存在"));

        WechatPipeline pipeline = paymentPipelineManager.findPipelineById(payment.getPipelineId(), WechatPipeline.class);
        WechatPrepayClose request = WechatPrepayClose.of(payment.getPaymentId());
        request.attach(WechatMerchant.of(payment.getWxMchId(), payment.getAppId()));
        pipeline.closePrepayOrder(request);
        WechatPaymentDTO paymentDTO = WechatPaymentDTO.builder().paymentId(payment.getPaymentId())
            .outTradeNo(null).openId(null).payTime(null).state(PaymentState.FAILED.getCode())
            .description("人工关闭微信订单").version(payment.getVersion()).modifiedTime(now).build();
        if (wechatPaymentDao.compareAndSetState(paymentDTO) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        TradePayment tradePayment = TradePayment.builder().paymentId(payment.getPaymentId()).tradeId(trade.getTradeId())
            .channelId(ChannelType.WXPAY.getCode()).payType(payment.getPayType()).accountId(payment.getAccountId())
            .name(payment.getName()).amount(payment.getAmount()).fee(0L).protocolId(null).cycleNo(null)
            .state(PaymentState.FAILED.getCode()).description("人工关闭微信订单").version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);

        TradeStateDTO tradeStateDTO = TradeStateDTO.of(trade.getTradeId(), TradeState.CLOSED.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeStateDTO) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatRefundResult sendRefundRequest(WechatPayment payment, WechatRefundDTO request) {
        List<Fee> fees = request.fees().orElse(Collections.emptyList());
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != request.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际退款金额与申请退款金额不一致");
        }

        MerchantPermit merchant = accessPermitService.loadMerchantPermit(payment.getMchId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder trade = tradeOrderDao.findByTradeId(partition, payment.getTradeId()).get();
        if (!TradeState.forRefund(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "无效的交易状态，不能进行交易退款");
        }
        if (trade.getAmount() < request.getAmount()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "申请退费金额超过原支付金额");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        KeyGenerator refundIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String refundId = refundIdKey.nextId();
        WechatPipeline pipeline = paymentPipelineManager.findPipelineByMchId(payment.getPipelineId(), WechatPipeline.class);
        WechatRefundRequest refundRequest = WechatRefundRequest.of(refundId, payment.getPaymentId(), trade.getMaxAmount(),
            request.getAmount(), request.getDescription(), now);
        WechatRefundResponse refundResponse = pipeline.sendRefundRequest(refundRequest);
        PaymentState refundState = WechatStateUtils.getRefundState(refundResponse.getState());

        WechatPayment refund = WechatPayment.builder().mchId(payment.getMchId()).wxMchId(payment.getWxMchId())
            .appId(payment.getAppId()).tradeId(payment.getTradeId()).type(TradeType.REFUND_TRADE.getCode())
            .paymentId(refundId).payType(payment.getPayType()).pipelineId(payment.getPipelineId())
            .accountId(payment.getAccountId()).name(payment.getName()).goods(payment.getGoods() + "-退款")
            .amount(request.getAmount()).objectId(payment.getPaymentId()).openId(payment.getOpenId())
            .payTime(refundRequest.getWhen()).outTradeNo(refundResponse.getOutTradeNo()).state(refundState.getCode())
            .notifyUri(request.getNotifyUri()).description(request.getDescription()).version(0).createdTime(now)
            .modifiedTime(now).build();
        wechatPaymentDao.insertWechatPayment(refund);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(refundId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(partition, paymentFeeDos);

        if (refundState == PaymentState.SUCCESS || refundState == PaymentState.FAILED) {
            RefundPayment refundPayment = RefundPayment.builder().paymentId(refundId).type(TradeType.REFUND_TRADE.getCode())
                .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(ChannelType.WXPAY.getCode())
                .amount(refund.getAmount()).fee(0L).cycleNo(request.getCycleNo()).state(refundState.getCode())
                .version(0).createdTime(now).modifiedTime(now).build();
            refundPaymentDao.insertRefundPayment(refundPayment);

            if (refundState == PaymentState.SUCCESS) {
                Long newAmount = trade.getAmount() - refund.getAmount();
                TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
                    trade.getVersion(), now);
                if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
                    throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
                }

                AccountPipeline accountPipeline = AccountPipeline.of(merchant.getProfitAccount());
                TransactionBuilder transaction = accountPipeline.openTransaction(refundId, TradeType.REFUND_TRADE.getCode(), now);
                fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
                accountPipelineService.submitExclusively(transaction::build);
            }
        } else {
            // 十分钟兜底处理微信退款订单
            messageQueueService.sendWechatScanMessage(TaskMessage.of(TaskMessage.TYPE_WECHAT_REFUND_SCAN, refundId));
        }

        return new WechatRefundResult(refundId, payment.getPaymentId(), refundState.getCode(),
            refundResponse.getWhen(), refundResponse.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyRefundResult(WechatRefundResponse response) {
        WechatPayment refund = response.getObject("refund", WechatPayment.class);
        WechatPayment payment = response.getObject("payment", WechatPayment.class);
        PaymentState refundState = WechatStateUtils.getRefundState(response.getState());
        if (refundState != PaymentState.SUCCESS && refundState != PaymentState.FAILED) {
            LOG.warn("Ignore wechat refund notification for {}:{}", payment.getPaymentId(), response.getState());
            return;
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(refund.getMchId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder trade = tradeOrderDao.findByTradeId(partition, refund.getTradeId()).get();

        RefundPayment refundPayment = RefundPayment.builder().paymentId(refund.getPaymentId()).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).channelId(ChannelType.WXPAY.getCode())
                // TODO: miss cycleNo
            .amount(refund.getAmount()).fee(0L).cycleNo(null).state(refundState.getCode())
            .version(0).createdTime(now).modifiedTime(now).build();
        refundPaymentDao.insertRefundPayment(refundPayment);

        if (refundState == PaymentState.SUCCESS) {
            Long newAmount = trade.getAmount() - refund.getAmount();
            TradeStateDTO tradeState = TradeStateDTO.of(trade.getTradeId(), newAmount, TradeState.REFUND.getCode(),
                trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(partition, tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
            }

            List<PaymentFee> fees = paymentFeeDao.findPaymentFees(partition, refund.getPaymentId());
            AccountPipeline accountPipeline = AccountPipeline.of(merchant.getProfitAccount());
            TransactionBuilder transaction = accountPipeline.openTransaction(refund.getPaymentId(), TradeType.REFUND_TRADE.getCode(), now);
            fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            accountPipelineService.submitExclusively(transaction::build);
        }

        messageQueueService.sendWechatNotifyMessage(refund.getNotifyUri(), new WechatRefundResult(refund.getPaymentId(),
            payment.getPaymentId(), refundState.getCode(), response.getWhen(), response.getMessage()));
    }
}
