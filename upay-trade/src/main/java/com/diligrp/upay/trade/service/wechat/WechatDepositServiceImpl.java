package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.domain.TransactionStatus;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.service.IUserAccountService;
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
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.dao.ITradePaymentDao;
import com.diligrp.upay.trade.domain.TradeStateDTO;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.message.MessageQueueService;
import com.diligrp.upay.trade.message.TaskMessage;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.model.TradePayment;
import com.diligrp.upay.trade.service.IWechatDepositService;
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

@Service("wechatDepositService")
public class WechatDepositServiceImpl implements IWechatDepositService {

    private static final Logger LOG = LoggerFactory.getLogger(WechatDepositServiceImpl.class);

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private IWechatPaymentDao wechatPaymentDao;

    @Resource
    private IUserAccountService userAccountService;

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
        MerchantPermit merchant = application.getMerchant();
        WechatPipeline pipeline = paymentPipelineManager.findPipelineByMchId(merchant.getMchId(), WechatPipeline.class);
        if (pipeline instanceof WechatPartnerPipeline) {
            AssertUtils.notEmpty(request.getMchId(), "参数错误: 未提供子商户信息");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        UserAccount account = userAccountService.findUserAccountById(merchant.parentMchId(), request.getAccountId());
        KeyGenerator paymentIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.PAYMENT_ID);
        String paymentId = String.valueOf(paymentIdKey.nextId());
        WechatPrepayRequest prepayRequest = WechatPrepayRequest.of(paymentId, request.getOpenId(), request.getAmount(),
            request.getGoods(), request.getDescription(), now);
        prepayRequest.attach(WechatMerchant.of(request.getMchId(), request.getAppId()));
        WechatPrepayResponse prepayResponse = switch (paymentType) {
            case JSAPI -> pipeline.sendJsApiPrepayRequest(prepayRequest);
            case NATIVE -> pipeline.sendNativePrepayRequest(prepayRequest);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "充值业务不支持此类微信支付方式");
        };

        KeyGenerator tradeIdKey = snowflakeKeyManager.getKeyGenerator(SnowflakeKey.TRADE_ID);
        String tradeId = String.valueOf(tradeIdKey.nextId());
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        TradeOrder tradeOrder = TradeOrder.builder().mchId(merchant.getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(TradeType.ONLINE_DEPOSIT.getCode()).outTradeNo(request.getOutTradeNo())
            .accountId(account.getAccountId()).name(account.getName()).amount(request.getAmount())
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
            .tradeId(tradeId).type(TradeType.ONLINE_DEPOSIT.getCode()).paymentId(paymentId).payType(paymentType.getCode())
            .pipelineId(pipeline.pipelineId()).accountId(account.getAccountId()).name(account.getName())
            .goods(request.getGoods()).amount(request.getAmount()).objectId(objectId).openId(null)
            .payTime(null).outTradeNo(null).state(PaymentState.PENDING.getCode()).notifyUri(request.getNotifyUri())
            .description(null).version(0).createdTime(now).modifiedTime(now).build();
        wechatPaymentDao.insertWechatPayment(payment);

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
        UserAccount account = userAccountService.findUserAccountById(payment.getAccountId());
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

        AccountPipeline pipeline = AccountPipeline.of(account);
        TransactionBuilder transaction = pipeline.openTransaction(payment.getPaymentId(), TradeType.ONLINE_DEPOSIT.getCode(), now);
        transaction.income(payment.getAmount(), 0, "充值金额", "账户充值");
        TransactionStatus status = accountPipelineService.submit(transaction::build);

        TradePayment tradePayment = TradePayment.builder().paymentId(payment.getPaymentId()).tradeId(trade.getTradeId())
            .channelId(ChannelType.WXPAY.getCode()).payType(payment.getPayType()).accountId(account.getAccountId())
            .name(account.getName()).amount(payment.getAmount()).fee(0L).protocolId(null).cycleNo(null)
            .state(paymentState.getCode()).description(response.getMessage()).version(0).createdTime(now).modifiedTime(now).build();
        tradePaymentDao.insertTradePayment(partition, tradePayment);

        TradeState tradeState = paymentState == PaymentState.SUCCESS ? TradeState.SUCCESS : TradeState.FAILED;
        TradeStateDTO tradeStateDTO = TradeStateDTO.of(trade.getTradeId(), tradeState.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(partition, tradeStateDTO) == 0) {
            throw new TradePaymentException(ErrorCode.SYSTEM_BUSY_ERROR, ErrorCode.MESSAGE_SYSTEM_BUSY);
        }

        DepositPaymentResult paymentResult = new DepositPaymentResult(payment.getPaymentId(), paymentState.getCode(),
            trade.getOutTradeNo(), status, now, response.getMessage());
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

    private static class DepositPaymentResult extends WechatPaymentResult {
        private TransactionStatus status;

        public DepositPaymentResult(String paymentId, int state, String outTradeNo, TransactionStatus status,
                                    LocalDateTime when, String message) {
            super(paymentId, state, outTradeNo, when, message);
            this.status = status;
        }

        public TransactionStatus getStatus() {
            return status;
        }
    }
}
