package com.diligrp.upay.trade.dao;

import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.trade.model.PaymentFee;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付费用数据访问层
 */
@Repository("paymentFeeDao")
public interface IPaymentFeeDao extends MybatisMapperSupport {
    void insertPaymentFee(@Param("strategy") DataPartition strategy, @Param("fee") PaymentFee fee);

    void insertPaymentFees(@Param("strategy") DataPartition strategy, @Param("fees") List<PaymentFee> fees);

    List<PaymentFee> findPaymentFees(@Param("strategy") DataPartition strategy, @Param("paymentId") String paymentId);
}
