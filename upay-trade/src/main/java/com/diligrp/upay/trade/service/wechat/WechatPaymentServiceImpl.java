package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.pipeline.dao.IWechatPaymentDao;
import com.diligrp.upay.pipeline.domain.WechatPaymentResponse;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.domain.WechatRefundResponse;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.diligrp.upay.trade.dao.ITradeOrderDao;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.service.IWechatDepositService;
import com.diligrp.upay.trade.service.IWechatFeeService;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import com.diligrp.upay.trade.service.IWechatTradeService;
import com.diligrp.upay.trade.type.PaymentState;
import com.diligrp.upay.trade.type.TradeType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 微信支付服务实现
 */
@Service("wechatPaymentService")
public class WechatPaymentServiceImpl implements IWechatPaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(WechatPaymentServiceImpl.class);

    @Resource
    private IWechatDepositService wechatDepositService;

    @Resource
    private IWechatFeeService wechatFeeService;

    @Resource
    private IWechatTradeService wechatTradeService;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IWechatPaymentDao wechatPaymentDao;

    @Override
    public WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request) {
        MerchantPermit merchant = application.getMerchant();
        DataPartition partition = DataPartition.strategy(merchant.parentMchId());
        if (tradeOrderDao.findByOutTradeNo(partition, merchant.getMchId(), request.getOutTradeNo()).isPresent()) {
            throw new TradePaymentException(ErrorCode.OBJECT_ALREADY_EXISTS, "Duplicate outTradeNo");
        }

        if (TradeType.ONLINE_DEPOSIT.equalTo(request.getType())) {
            return wechatDepositService.prepay(application, request);
        } else if (TradeType.ONLINE_FEE.equalTo(request.getType())) {
            return wechatFeeService.prepay(application, request);
        } else if (TradeType.ONLINE_TRADE.equalTo(request.getType())) {
            return wechatTradeService.prepay(application, request);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信支付");
        }
    }

    @Override
    public void notifyPaymentResult(WechatPaymentResponse response) {
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(response.getPaymentId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信支付订单不存在"));
        if (!PaymentState.PENDING.equalTo(payment.getState())) { // 订单查询接口已更改了状态，则直接返回结果
            LOG.warn("Duplicate processing wechat prepay order: [{},{}]", response.getPaymentId(), response.getState());
            return;
        }
        response.attach(payment);
        LOG.debug("Processing wechat prepay order result notify: [{},{}]", response.getPaymentId(), response.getState());

        if (TradeType.ONLINE_DEPOSIT.equalTo(payment.getType())) {
            wechatDepositService.notifyPaymentResult(response);
        } else if (TradeType.ONLINE_FEE.equalTo(payment.getType())) {
            wechatFeeService.notifyPaymentResult(response);
        } else if (TradeType.ONLINE_TRADE.equalTo(payment.getType())) {
            wechatTradeService.notifyPaymentResult(response);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信支付");
        }
    }

    @Override
    public void closePrepayOrder(ApplicationPermit application, String paymentId) {
        Optional<WechatPayment> opt = wechatPaymentDao.findByPaymentId(paymentId);
        WechatPayment payment = opt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信支付订单不存在"));
        if (!ObjectUtils.equals(payment.getMchId(), application.getMerchant().getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作该商户下的微信订单");
        }
        if (!PaymentState.PENDING.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "不能关闭微信订单: 无效的订单状态");
        }
        LOG.debug("Closing wechat prepay order: {}:{}", paymentId, payment.getState());

        if (TradeType.ONLINE_DEPOSIT.equalTo(payment.getType())) {
            wechatDepositService.closePrepayOrder(payment);
        } else if (TradeType.ONLINE_FEE.equalTo(payment.getType())) {
            wechatFeeService.closePrepayOrder(payment);
        } else if (TradeType.ONLINE_TRADE.equalTo(payment.getType())) {
            wechatTradeService.closePrepayOrder(payment);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能关闭微信订单: 不支持的交易类型");
        }
    }

    @Override
    public WechatRefundResult sendRefundRequest(ApplicationPermit application, WechatRefundDTO request) {
        Optional<WechatPayment> paymentOpt = wechatPaymentDao.findByPaymentId(request.getPaymentId());
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "微信支付订单不存在"));
        if (!ObjectUtils.equals(application.getMerchant().parentMchId(), payment.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "商户没有权限操作该交易订单");
        }
        if (!PaymentState.SUCCESS.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_OBJECT_STATE, "不能进行交易退款: 无效的交易状态");
        }

        if (TradeType.PAY_FEE.equalTo(payment.getType())) {
            return wechatFeeService.sendRefundRequest(payment, request);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信退款");
        }
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
        WechatPayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "原微信支付订单不存在"));
        response.attach("refund", refund).attach("payment", payment);
        if (TradeType.PAY_FEE.equalTo(payment.getType())) {
            wechatFeeService.notifyRefundResult(response);
        } else {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持微信退款");
        }
    }

    @Override
    public WechatPaymentResult queryPrepayOrder(ApplicationPermit application, String paymentId, String mode) {
        return null;
    }

    @Override
    public WechatRefundResult queryRefundOrder(ApplicationPermit application, String paymentId, String mode) {
        return null;
    }

    @Override
    public void deliverGoods(String paymentId, int logisticsType) {

    }

    @Override
    public String loginAuthorization(ApplicationPermit application, String code) {
        return null;
    }

    @Override
    public void scanWechatPrepayOrder(String paymentId) {

    }

    @Override
    public void scanWechatRefundOrder(String paymentId) {

    }
}
