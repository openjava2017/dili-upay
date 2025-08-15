package com.diligrp.upay.boot.component;

import com.diligrp.upay.boot.domain.wechat.DeliverGoods;
import com.diligrp.upay.boot.domain.wechat.OpenId;
import com.diligrp.upay.boot.domain.wechat.PaymentId;
import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.SumPageMessage;
import com.diligrp.upay.pipeline.domain.WechatPrepayOrder;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.domain.WechatRefundOrder;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementDTO;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementQuery;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import jakarta.annotation.Resource;

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
        AssertUtils.notNull(prepayRequest.getType(), "type missed");
        // 基本参数校验
        AssertUtils.notNull(prepayRequest.getPayType(), "payType missed");
        AssertUtils.notNull(prepayRequest.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(prepayRequest.getGoods(), "goods missed");
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

    public WechatPaymentResult state(ServiceRequest<PaymentId> request) {
        PaymentId paymentId = request.getData();
        AssertUtils.notEmpty(paymentId.getPaymentId(), "paymentId missed");
        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);

        WechatPrepayOrder order = WechatPrepayOrder.of(paymentId.getPaymentId());
        return wechatPaymentService.queryPrepayOrder(application, order, paymentId.getMode());
    }

    /**
     * 关闭预支付订单
     */
    public void close(ServiceRequest<PaymentId> request) {
        PaymentId paymentId = request.getData();
        AssertUtils.notEmpty(paymentId.getPaymentId(), "paymentId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        wechatPaymentService.closePrepayOrder(permit, paymentId.getPaymentId());
    }

    public WechatRefundResult refund(ServiceRequest<WechatRefundDTO> request) {
        WechatRefundDTO refund = request.getData();
        AssertUtils.notEmpty(refund.getPaymentId(), "paymentId missed");
        AssertUtils.notNull(refund.getAmount(), "amount missed");
        AssertUtils.isTrue(refund.getAmount() > 0, "Invalid amount");

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

    public WechatRefundResult refundState(ServiceRequest<PaymentId> request) {
        PaymentId refundId = request.getData();
        AssertUtils.notEmpty(refundId.getRefundId(), "refundId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);

        WechatRefundOrder order = WechatRefundOrder.of(refundId.getRefundId());
        return wechatPaymentService.queryRefundOrder(permit, order, refundId.getMode());
    }

    public OpenId openId(ServiceRequest<OpenId> request) {
        //TODO: 测试get请求直接拿参数(request.getContext().getString("code"))，并非所有参数传递都要使用post请求，可以减少DTO数量
        OpenId data = request.getData();
        AssertUtils.notEmpty(data.getCode(), "code missed");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        String openId = wechatPaymentService.loginAuthorization(permit, data.getCode());
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
