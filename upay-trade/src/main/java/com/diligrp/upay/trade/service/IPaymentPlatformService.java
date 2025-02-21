package com.diligrp.upay.trade.service;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.trade.domain.*;

/**
 * 支付平台服务接口类
 */
public interface IPaymentPlatformService {

    /**
     * 提交交易支付：适用于所有交易，不同的交易类型有不同的业务处理逻辑
     *
     * @param application - 应用接入许可
     * @param request - 支付请求
     * @return 支付结果
     */
    PaymentResult commit(ApplicationPermit application, TradeDTO request);

    /**
     * 确认预授权消费：适用于预授权业务（预授权缴费和预授权交易）
     *
     * @param application - 应用接入许可
     * @param request - 确认预授权申请
     * @return 支付结果
     */
    PaymentResult confirm(ApplicationPermit application, ConfirmDTO request);

    /**
     * 交易退款：支持部分退款或全额退款
     *
     * @param application - 应用接入许可
     * @param request - 退款交易申请
     * @return 支付结果
     */
    PaymentResult refund(ApplicationPermit application, RefundDTO request);

    /**
     * 撤销交易：撤销预授权业务时将解冻冻结资金，撤销普通业务时将进行资金逆向操作
     *
     * @param application - 应用接入许可
     * @param request - 撤销交易申请
     * @return 支付结果
     */
    PaymentResult cancel(ApplicationPermit application, RefundDTO request);

    /**
     * 交易冲正：目前只有充值、提现允许进行交易冲正
     *
     * @param application - 应用接入许可
     * @param request - 交易冲正申请
     * @return 处理结果
     */
    PaymentResult correct(ApplicationPermit application, CorrectDTO request);
}
