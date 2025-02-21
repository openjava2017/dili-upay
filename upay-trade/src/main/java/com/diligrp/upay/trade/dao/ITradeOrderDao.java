package com.diligrp.upay.trade.dao;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.trade.domain.TradeStateDTO;
import com.diligrp.upay.trade.model.TradeOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 交易订单数据访问层
 */
@Repository("tradeOrderDao")
public interface ITradeOrderDao extends MybatisMapperSupport {
    void insertTradeOrder(@Param("strategy") DataPartition strategy, @Param("tradeOrder") TradeOrder tradeOrder);

    Optional<TradeOrder> findByTradeId(@Param("strategy") DataPartition strategy, @Param("tradeId") String tradeId);

    Optional<TradeOrder> findByOutTradeNo(@Param("strategy") DataPartition strategy, @Param("mchId") Long mchId, @Param("outTradeNo") String outTradeNo);

    int compareAndSetState(@Param("strategy") DataPartition strategy, @Param("tradeState") TradeStateDTO tradeState);
}