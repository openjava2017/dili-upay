package com.diligrp.upay.boot.component;

import com.diligrp.upay.boot.domain.wechat.DeliverGoods;
import com.diligrp.upay.boot.domain.wechat.OpenId;
import com.diligrp.upay.boot.domain.wechat.PrepayId;
import com.diligrp.upay.boot.domain.wechat.RefundId;
import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.SumPageMessage;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementDTO;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementQuery;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import com.diligrp.upay.trade.type.TradeType;
import jakarta.annotation.Resource;

import java.util.Optional;

/**
 * 微信支付服务组件
 */
@CallableComponent(id = "wechat.payment.service")
public class WechatServiceComponent {

    @Resource
    private IWechatPaymentService wechatPaymentService;

    /**
     * 微信预支付
     */
    public WechatPrepayResponse prepare(ServiceRequest<WechatPrepayDTO> request) {
        WechatPrepayDTO prepayRequest = request.getData();
        Optional<TradeType> tradeType = TradeType.getType(prepayRequest.getType());
        tradeType.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的交易类型"));
        // 基本参数校验
        AssertUtils.notNull(prepayRequest.getPayType(), "payType missed");
        AssertUtils.notNull(prepayRequest.getAccountId(), "accountId missed");
        AssertUtils.notNull(prepayRequest.getGoods(), "goods missed");
        AssertUtils.notNull(prepayRequest.getAmount(), "amount missed");
        AssertUtils.isTrue(prepayRequest.getAmount() > 0, "Invalid amount");
        AssertUtils.notEmpty(prepayRequest.getOutTradeNo(), "outTradeNo missed");
        AssertUtils.notEmpty(prepayRequest.getNotifyUri(), "notifyUri missed");

        // 费用参数校验
        prepayRequest.fees().ifPresent(fees -> fees.stream().forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));
        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);
        return wechatPaymentService.prepay(application, prepayRequest);
    }

    public WechatPaymentResult state(ServiceRequest<PrepayId> request) {
        PrepayId prepayId = request.getData();
        AssertUtils.notEmpty(prepayId.getPaymentId(), "paymentId missed");
        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);

        return wechatPaymentService.queryPrepayOrder(application, prepayId.getPaymentId(), prepayId.getMode());
    }

    /**
     * 关闭预支付订单
     */
    public void close(ServiceRequest<PrepayId> request) {
        PrepayId prepayId = request.getData();
        AssertUtils.notEmpty(prepayId.getPaymentId(), "paymentId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        wechatPaymentService.closePrepayOrder(permit, prepayId.getPaymentId());
    }

    public WechatRefundResult refund(ServiceRequest<WechatRefundDTO> request) {
        WechatRefundDTO refund = request.getData();
        AssertUtils.notEmpty(refund.getPaymentId(), "paymentId missed");
        AssertUtils.notNull(refund.getAmount(), "amount missed");

        // 费用参数校验
        refund.fees().ifPresent(fees -> fees.stream().forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        return wechatPaymentService.sendRefundRequest(permit, refund);
    }

    public WechatRefundResult refundState(ServiceRequest<RefundId> request) {
        RefundId refundId = request.getData();
        AssertUtils.notEmpty(refundId.getRefundId(), "refundId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        return wechatPaymentService.queryRefundOrder(permit, refundId.getRefundId(), refundId.getMode());
    }

    public OpenId openId(ServiceRequest<OpenId> request) {
        OpenId userCode = request.getData();
        AssertUtils.notEmpty(userCode.getCode(), "code missed");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        String openId = wechatPaymentService.loginAuthorization(permit, userCode.getCode());
        return OpenId.of(openId);
    }

    public void deliver(ServiceRequest<DeliverGoods> request) {
        DeliverGoods deliverGoods = request.getData();
        Integer logisticsType = deliverGoods.getLogisticsType();
        AssertUtils.notEmpty(deliverGoods.getPaymentId(), "paymentId missed");
        AssertUtils.notNull(logisticsType, "logisticsType missed");
        AssertUtils.isTrue(logisticsType == 2 || logisticsType == 4, "Invalid logisticsType");

        wechatPaymentService.deliverGoods(deliverGoods.getPaymentId(), deliverGoods.getLogisticsType());
    }

    /**
     * 查询微信支付流水
     */
    public SumPageMessage<WechatStatementDTO> statement(ServiceRequest<WechatStatementQuery> request) {
        WechatStatementQuery query = request.getData();
        query.from(query.getPageNo(), query.getPageSize());

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        query.setMchId(permit.getMerchant().getMchId());

        return wechatPaymentService.listWechatStatements(query);
    }
}
