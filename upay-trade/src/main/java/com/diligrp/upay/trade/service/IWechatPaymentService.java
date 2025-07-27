package com.diligrp.upay.trade.service;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementDTO;
import com.diligrp.upay.pipeline.domain.wechat.WechatStatementQuery;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;

/**
 * 微信支付服务接口
 */
public interface IWechatPaymentService {

    /**
     * 微信支付预支付下单
     */
    WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request);

    /**
     * 微信支付完成回调通知
     */
    void notifyPaymentResult(WechatPaymentResponse response);

    /**
     * 查询微信预支付订单状态
     */
    WechatPaymentResult queryPrepayOrder(ApplicationPermit application, WechatPrepayOrder order, String mode);

    /**
     * 关闭微信预支付订单
     */
    void closePrepayOrder(ApplicationPermit application, String paymentId);

    /**
     * 微信退款申请
     */
    WechatRefundResult sendRefundRequest(ApplicationPermit application, WechatRefundDTO request);

    /**
     * 微信退款完成回调通知
     */
    void notifyRefundResult(WechatRefundResponse response);


    /**
     * 查询微信退款订单状态
     */
    WechatRefundResult queryRefundOrder(ApplicationPermit application, WechatRefundOrder order, String mode);

    /**
     *  通知微信发货
     *  服务商模式下，解决买家微信付款成功，卖家微信商户号无法收到钱，需要去自己的微信后台操作一下【发货】
     */
    void deliverGoods(String paymentId, int logisticsType);

    String loginAuthorization(ApplicationPermit application, String code);

    /**
     * 分页查询微信支付流水
     */
    SumPageMessage<WechatStatementDTO> listWechatStatements(WechatStatementQuery query);

    /**
     * 扫描微信支付申请 - 根据微信订单查询结果，进行关闭或完成本地支付订单
     */
    void scanWechatPrepayOrder(WechatPrepayOrder order);

    /**
     * 扫描微信退款申请 - 根据微信退款查询结果，完成退款订单
     */
    void scanWechatRefundOrder(WechatRefundOrder order);
}
