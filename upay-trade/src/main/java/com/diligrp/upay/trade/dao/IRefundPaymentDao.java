package com.diligrp.upay.trade.dao;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.trade.domain.PaymentStateDTO;
import com.diligrp.upay.trade.model.RefundPayment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 退款数据访问层
 */
@Repository("refundPaymentDao")
public interface IRefundPaymentDao extends MybatisMapperSupport {
    void insertRefundPayment(RefundPayment payment);

    Optional<RefundPayment> findByPaymentId(String paymentId);

    List<RefundPayment> findRefundPayments(String tradeId);

    int compareAndSetState(PaymentStateDTO paymentState);
}