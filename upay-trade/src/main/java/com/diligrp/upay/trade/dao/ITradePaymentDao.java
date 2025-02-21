package com.diligrp.upay.trade.dao;

import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.trade.domain.PaymentStateDTO;
import com.diligrp.upay.trade.model.TradePayment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交易支付数据访问层
 */
@Repository("tradePaymentDao")
public interface ITradePaymentDao extends MybatisMapperSupport {
    void insertTradePayment(@Param("strategy") DataPartition strategy, @Param("payment") TradePayment payment);

    Optional<TradePayment> findByPaymentId(@Param("strategy") DataPartition strategy, @Param("paymentId") String paymentId);

    List<TradePayment> listByTradeId(@Param("strategy") DataPartition strategy, @Param("tradeId") String tradeId);

    Optional<TradePayment> findByTradeId(@Param("strategy") DataPartition strategy, @Param("tradeId") String tradeId);

    int compareAndSetState(@Param("strategy") DataPartition strategy, @Param("paymentState") PaymentStateDTO paymentState);
}