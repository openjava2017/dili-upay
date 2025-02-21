package com.diligrp.upay.trade.service;

import com.diligrp.upay.shared.domain.PageMessage;
import com.diligrp.upay.trade.domain.FreezeFundDTO;
import com.diligrp.upay.trade.domain.FrozenAmount;
import com.diligrp.upay.trade.domain.FrozenOrderQuery;
import com.diligrp.upay.trade.domain.FrozenStatus;
import com.diligrp.upay.trade.model.FrozenOrder;

import java.util.Optional;

/**
 * 资金冻结/解冻订单服务接口
 */
public interface IFrozenOrderService {
    /**
     * 资金冻结
     */
    FrozenStatus freeze(FreezeFundDTO request);

    /**
     * 资金解冻
     */
    FrozenStatus unfreeze(String paymentId);

    /**
     * 分页查询冻结/解冻订单
     */
    PageMessage<FrozenOrder> listFrozenOrders(FrozenOrderQuery query);

    /**
     * 查询冻结金额详情-人工冻结和交易冻结金额
     */
    Optional<FrozenAmount> findFrozenAmount(Long accountId);
}
