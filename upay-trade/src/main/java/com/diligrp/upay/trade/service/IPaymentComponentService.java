package com.diligrp.upay.trade.service;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.model.TradeOrder;
import com.diligrp.upay.trade.type.TradeType;

/**
 * 支付服务接口
 */
public interface IPaymentComponentService {
    /**
     * 提交支付
     */
    PaymentResult commit(Trade trade, Payment payment);

    /**
     * "预授权交易"确认预授权消费(交易冻结后确认实际缴费金额)，当前业务场景允许实际缴费金额大于冻结金额
     *
     * 预授权交易业务场景处理经历三个阶段: prepare(创建交易)->commit(冻结资金)->confirm/cancel(确认消费/撤销冻结)
     */
    default PaymentResult confirm(TradeOrder trade, Confirm confirm) {
        throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持确认消费操作");
    }

    /**
     * 交易退款-退部分金额，交易退款不需要修改交易订单状态
     */
    default PaymentResult refund(TradeOrder trade, Refund refund) {
        throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持退款操作");
    }

    /**
     * 撤销交易-退所有金额，交易撤销需要修改交易订单状态
     */
    default PaymentResult cancel(TradeOrder trade, Refund cancel) {
        throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持撤销操作");
    }

    /**
     * 交易冲正，不需要修改源订单状态
     */
    default PaymentResult correct(TradeOrder trade, Correct correct) {
        throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "该交易不支持冲正操作");
    }

    TradeType supportType();
}