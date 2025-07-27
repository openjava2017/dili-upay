package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.pipeline.client.WechatHttpClient;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.pipeline.dao.IWechatPaymentDao;
import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.*;
import com.diligrp.upay.pipeline.impl.WechatPartnerPipeline;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.service.IWechatDepositService;
import com.diligrp.upay.trade.service.IWechatFeeService;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import com.diligrp.upay.trade.service.IWechatTradeService;
import com.diligrp.upay.trade.type.PaymentState;
import com.diligrp.upay.trade.type.TradeType;
import com.diligrp.upay.trade.util.WechatStateUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 微信支付服务实现
 */
@Service("wechatPaymentService")
public class WechatPaymentServiceImpl implements IWechatPaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(WechatPaymentServiceImpl.class);

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IWechatPaymentDao wechatPaymentDao;

    @Resource
    private IWechatDepositService wechatDepositService;

    @Resource
    private IWechatFeeService wechatFeeService;

    @Resource
    private IWechatTradeService wechatTradeService;

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    @Override
    public WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request) {
        TradeType tradeType = TradeType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "系统不支持此类交易类型"));
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        if (tradeOrderDao.findByOutTradeNo(partition, merchant.getMchId(), request.getOutTradeNo()).isPresent()) {
            throw new TradePaymentException(ErrorCode.OBJECT_ALREADY_EXISTS, "Duplicate outTradeNo");
        }

        return switch (tradeType) {
            case ONLINE_DEPOSIT -> wechatDepositService.prepay(application, request);
            case ONLINE_FEE -> wechatFeeService.prepay(application, request);
            case ONLINE_TRADE -> wechatTradeService.prepay(application, request);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信支付");
        };
    }

    @Override
    public void notifyPaymentResult(WechatPaymentResponse response) {
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(response.getPaymentId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        if (!PaymentState.PENDING.equalTo(payment.getState())) { // 订单查询接口已更改了状态，则直接返回结果
            LOG.warn("Duplicate processing wechat prepay order: [{},{}]", response.getPaymentId(), response.getState());
            return;
        }
        response.attach(payment);
        LOG.debug("Processing wechat prepay order result notify: [{},{}]", response.getPaymentId(), response.getState());

        TradeType tradeType = TradeType.getType(payment.getType()).get();
        switch (tradeType) {
            case ONLINE_DEPOSIT -> wechatDepositService.notifyPaymentResult(response);
            case ONLINE_FEE -> wechatFeeService.notifyPaymentResult(response);
            case ONLINE_TRADE -> wechatTradeService.notifyPaymentResult(response);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信支付");
        }
    }

    @Override
    public WechatPaymentResult queryPrepayOrder(ApplicationPermit application, WechatPrepayOrder order, String mode) {
        String paymentId = order.getPaymentId();
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(paymentId);
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findByTradeId(partition, payment.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付订单不存在"));
        if (!ObjectUtils.equals(payment.getMchId(), application.getMerchant().getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作该商户下的微信订单");
        }

        LOG.debug("Query wechat prepay order[{}-{}] state...", paymentId, payment.getState());
        // 微信支付通知较为及时和安全，非特殊情况可使用offline模式；一些本地状态与微信状态不一致的"异常订单"可使用online模式同步状态
        if (PaymentState.PENDING.equalTo(payment.getState()) && "online".equalsIgnoreCase(mode)) {
            WechatPipeline pipeline = paymentPipelineManager.findPipelineById(payment.getPipelineId(), WechatPipeline.class);
            order.attach(WechatMerchant.of(payment.getWxMchId(), payment.getAppId())); // 服务商模式还需子商户ID
            WechatPaymentResponse response = pipeline.queryPrepayResponse(order);
            PaymentState state = WechatStateUtils.getPaymentState(response.getState());
            return new WechatPaymentResult(paymentId, state.getCode(), trade.getOutTradeNo(), response.getWhen(), response.getMessage());
        }
        return new WechatPaymentResult(paymentId, payment.getState(), trade.getOutTradeNo(), payment.getPayTime(), payment.getDescription());
    }

    @Override
    public void closePrepayOrder(ApplicationPermit application, String paymentId) {
        Optional<WechatPayment> opt = wechatPaymentDao.findByPaymentId(paymentId);
        WechatPayment payment = opt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        if (!ObjectUtils.equals(payment.getMchId(), application.getMerchant().getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作该商户下的微信订单");
        }
        if (!PaymentState.PENDING.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "不能关闭微信订单: 无效的订单状态");
        }
        LOG.debug("Closing wechat prepay order: {}:{}", paymentId, payment.getState());

        WechatPrepayOrder order = WechatPrepayOrder.of(paymentId);
        order.attach(payment);
        TradeType tradeType = TradeType.getType(payment.getType()).get();
        switch (tradeType) {
            case ONLINE_DEPOSIT -> wechatDepositService.closePrepayOrder(order);
            case ONLINE_FEE -> wechatFeeService.closePrepayOrder(order);
            case ONLINE_TRADE -> wechatTradeService.closePrepayOrder(order);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能关闭微信订单: 不支持的交易类型");
        }
    }

    @Override
    public WechatRefundResult sendRefundRequest(ApplicationPermit application, WechatRefundDTO request) {
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(request.getPaymentId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        if (!ObjectUtils.equals(application.getMerchant().parentMchId(), payment.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "商户没有权限操作该交易订单");
        }
        if (!PaymentState.SUCCESS.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "不能进行交易退款: 无效的交易状态");
        }

        TradeType tradeType = TradeType.getType(payment.getType()).get();
        return switch (tradeType) {
            case ONLINE_FEE -> wechatFeeService.sendRefundRequest(payment, request);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信退款");
        };
    }

    @Override
    public void notifyRefundResult(WechatRefundResponse response) {
        Optional<WechatPayment> refundOpt = wechatPaymentDao.findByPaymentId(response.getRefundId());
        WechatPayment refund = refundOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信退款订单不存在"));
        if (!PaymentState.PENDING.equalTo(refund.getState())) {
            LOG.warn("Duplicate processing wechat refund order: [{},{}]", response.getRefundId(), response.getState());
            return;
        }
        LOG.debug("Processing wechat refund order result notify: [{},{}]", response.getRefundId(), response.getState());
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(refund.getObjectId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "原微信订单不存在"));
        response.attach("refund", refund).attach("payment", payment);

        TradeType tradeType = TradeType.getType(payment.getType()).get();
        switch (tradeType) {
            case ONLINE_FEE -> wechatFeeService.notifyRefundResult(response);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信退款");
        }
    }

    @Override
    public WechatRefundResult queryRefundOrder(ApplicationPermit application, WechatRefundOrder order, String mode) {
        String refundId = order.getRefundId();
        Optional<WechatPayment> refundOpt = wechatPaymentDao.findByPaymentId(refundId);
        WechatPayment refund = refundOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信退款订单不存在"));
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(refund.getObjectId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "原支付订单不存在"));
        if (!ObjectUtils.equals(refund.getMchId(), application.getMerchant().getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作该商户下的微信退款订单");
        }

        LOG.debug("Query wechat refund order[{}-{}] state...", refundId, refund.getState());
        // 微信支付通知较为及时和安全，非特殊情况可使用offline模式；一些本地状态与微信状态不一致的"异常订单"可使用online模式同步状态
        if (PaymentState.PENDING.equalTo(refund.getState()) && "online".equalsIgnoreCase(mode)) {
            WechatPipeline pipeline = paymentPipelineManager.findPipelineById(refund.getPipelineId(), WechatPipeline.class);
            order.attach(WechatMerchant.of(refund.getWxMchId(), refund.getAppId())); // 服务商模式还需子商户ID
            WechatRefundResponse response = pipeline.queryRefundResponse(order);
            PaymentState state = WechatStateUtils.getRefundState(response.getState());
            return new WechatRefundResult(refundId, payment.getPaymentId(), state.getCode(), response.getWhen(), response.getMessage());
        }
        return new WechatRefundResult(refundId, payment.getPaymentId(), refund.getState(), refund.getPayTime(), refund.getDescription());
    }

    @Override
    public void deliverGoods(String paymentId, int logisticsType) {
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(paymentId);
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        if (!PaymentState.SUCCESS.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "微信订单未完成，不能进行发货操作");
        }

        WechatPartnerPipeline pipeline = paymentPipelineManager.findPipelineById(payment.getPipelineId(), WechatPartnerPipeline.class);
        // 获取微信接口登录凭证，并调用微信发货信息录入接口
        WechatHttpClient client = pipeline.getClient();
        WechatAccessToken accessToken = client.getAccessToken();
        UploadShippingRequest request = UploadShippingRequest.of(payment.getOutTradeNo(), logisticsType,
            payment.getGoods(), payment.getOpenId());
        client.sendUploadShippingRequest(request, accessToken.getToken());
    }

    @Override
    public String loginAuthorization(ApplicationPermit application, String code) {
        MerchantPermit merchant = application.getMerchant();
        WechatPipeline pipeline = paymentPipelineManager.findPipelineByMchId(merchant.getMchId(), WechatPipeline.class);
        return pipeline.getClient().loginAuthorization(code);
    }

    @Override
    public SumPageMessage<WechatStatementDTO> listWechatStatements(WechatStatementQuery query) {
        SumWechatStatement sum = wechatPaymentDao.sumWechatStatements(query);
        if (sum != null && sum.getTotal() > 0) {
            List<WechatStatementDTO> statements = wechatPaymentDao.listWechatStatements(query);
            return SumPageMessage.success(sum.getTotal(), statements, sum.getIncome(), sum.getOutput());
        }

        return SumPageMessage.success(sum.getTotal(), Collections.emptyList(), 0, 0);
    }

    @Override
    public void scanWechatPrepayOrder(WechatPrepayOrder order) {
        String paymentId = order.getPaymentId();
        LOG.debug("scanPrepayOrder: processing wechat prepay order {}", paymentId);
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(paymentId);
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信订单不存在"));
        if (!PaymentState.PENDING.equalTo(payment.getState())) {
            return; // 微信已成功通知支付结果
        }

        WechatPipeline pipeline = paymentPipelineManager.findPipelineById(payment.getPipelineId(), WechatPipeline.class);
        order.attach(WechatMerchant.of(payment.getWxMchId(), payment.getAppId())); // 服务商模式还需子商户ID
        WechatPaymentResponse response = pipeline.queryPrepayResponse(order);
        if (WechatStateUtils.isPendingState(response.getState())) {
            try {
                order.attach(WechatMerchant.of(payment.getWxMchId(), payment.getAppId()));
                pipeline.closePrepayOrder(order);
                LOG.debug("scanPrepayOrder: close wechat prepay order {}", paymentId);
                response = WechatPaymentResponse.of(response.getPaymentId(), response.getOutTradeNo(), response.getOpenId(),
                    response.getWhen(), WechatConstants.STATE_CLOSED, "自动关闭超时的微信订单");
            } catch (Exception ex) {
                LOG.error("scanPrepayOrder: close wechat prepare order exception", ex);
            }
        }

        TradeType tradeType = TradeType.getType(payment.getType()).get();
        switch (tradeType) {
            case ONLINE_DEPOSIT -> wechatDepositService.notifyPaymentResult(response);
            case ONLINE_FEE -> wechatFeeService.notifyPaymentResult(response);
            case ONLINE_TRADE -> wechatTradeService.notifyPaymentResult(response);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信支付");
        }
    }

    @Override
    public void scanWechatRefundOrder(WechatRefundOrder order) {
        String refundId = order.getRefundId();
        LOG.debug("scanRefundOrder: processing wechat refund order {}", refundId);
        Optional<WechatPayment> refundOpt = wechatPaymentDao.findByPaymentId(refundId);
        WechatPayment refund = refundOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信退款订单不存在"));
        if (!PaymentState.PENDING.equalTo(refund.getState())) {
            return; // 微信已成功通知支付结果
        }

        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(refund.getObjectId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "原微信订单不存在"));
        WechatPipeline pipeline = paymentPipelineManager.findPipelineById(refund.getPipelineId(), WechatPipeline.class);
        order.attach(WechatMerchant.of(refund.getWxMchId(), refund.getAppId())); // 服务商模式还需子商户ID
        WechatRefundResponse response = pipeline.queryRefundResponse(order);
        response.attach("refund", refund).attach("payment", payment);

        TradeType tradeType = TradeType.getType(payment.getType()).get();
        switch (tradeType) {
            case ONLINE_FEE -> wechatFeeService.notifyRefundResult(response);
            default -> throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信退款");
        }
    }
}
