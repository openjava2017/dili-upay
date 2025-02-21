package com.diligrp.upay.boot.component;

import com.diligrp.upay.boot.domain.AccountId;
import com.diligrp.upay.boot.domain.PaymentId;
import com.diligrp.upay.boot.domain.FrozenOrderDTO;
import com.diligrp.upay.boot.domain.FundBalance;
import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IFundAccountService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.shared.domain.PageMessage;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.FreezeFundDTO;
import com.diligrp.upay.trade.domain.FrozenOrderQuery;
import com.diligrp.upay.trade.domain.FrozenStatus;
import com.diligrp.upay.trade.model.FrozenOrder;
import com.diligrp.upay.trade.service.IFrozenOrderService;
import com.diligrp.upay.trade.type.FrozenType;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资金服务组件
 */
@CallableComponent(id = "payment.fund.service")
public class FundServiceComponent {

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IFrozenOrderService frozenOrderService;

    /**
     * 系统冻结资金
     */
    public FrozenStatus freeze(ServiceRequest<FreezeFundDTO> request) {
        FreezeFundDTO freezeFund = request.getData();
        AssertUtils.notNull(freezeFund.getAccountId(), "accountId missed");
        AssertUtils.notNull(freezeFund.getAmount(), "amount missed");
        freezeFund.setType(FrozenType.SYSTEM_FROZEN.getCode());
        return frozenOrderService.freeze(freezeFund);
    }

    /**
     * 系统解冻资金
     */
    public FrozenStatus unfreeze(ServiceRequest<PaymentId> request) {
        PaymentId paymentId = request.getData();
        AssertUtils.notNull(paymentId.getPaymentId(), "paymentId missed");
        return frozenOrderService.unfreeze(paymentId.getPaymentId());
    }

    /**
     * 分页查询冻结订单
     */
    public PageMessage<FrozenOrderDTO> listFrozen(ServiceRequest<FrozenOrderQuery> request) {
        FrozenOrderQuery query = request.getData();
        AssertUtils.notNull(query.getAccountId(), "accountId missed");
        AssertUtils.isTrue(query.getPageNo() > 0, "invalid pageNum");
        AssertUtils.isTrue(query.getPageSize() > 0, "invalid pageSize");
        // 只查询系统冻结记录
        query.setType(FrozenType.SYSTEM_FROZEN.getCode());
        query.from(query.getPageNo(), query.getPageSize());
        PageMessage<FrozenOrder> result = frozenOrderService.listFrozenOrders(query);
        // 转化查询结果
        List<FrozenOrderDTO> frozenOrders = result.getData().stream().map(frozenOrder ->
            FrozenOrderDTO.of(frozenOrder.getPaymentId(), frozenOrder.getAccountId(), frozenOrder.getAmount(),
            frozenOrder.getState(), frozenOrder.getExtension(), frozenOrder.getCreatedTime(),
            frozenOrder.getModifiedTime(), frozenOrder.getDescription())
        ).collect(Collectors.toList());
        return PageMessage.success(result.getTotal(), frozenOrders);
    }

    /**
     * 查询账户余额
     */
    public FundBalance balance(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        UserAccount account = userAccountService.findUserAccountById(accountId.getAccountId());
        Long masterId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();

        FundAccount fund = fundAccountService.findFundAccountById(masterId);
        FundBalance balance = FundBalance.of(fund.getAccountId(), fund.getBalance(), fund.getFrozenAmount(), fund.getVouchAmount());
        balance.setState(account.getState());
        return balance;
    }
}