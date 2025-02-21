package com.diligrp.upay.trade.dao;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.trade.domain.FrozenAmount;
import com.diligrp.upay.trade.domain.FrozenOrderQuery;
import com.diligrp.upay.trade.domain.FrozenStateDTO;
import com.diligrp.upay.trade.model.FrozenOrder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 资金冻结订单数据访问
 */
@Repository("frozenOrderDao")
public interface IFrozenOrderDao extends MybatisMapperSupport {
    void insertFrozenOrder(FrozenOrder frozenOrder);

    Optional<FrozenOrder> findByPaymentId(String paymentId);

    int compareAndSetState(FrozenStateDTO frozenState);

    List<FrozenOrder> listFrozenOrders(FrozenOrderQuery query);

    long countFrozenOrders(FrozenOrderQuery query);

    Optional<FrozenAmount> findFrozenAmount(Long accountId);
}