package com.diligrp.upay.boot.component;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.*;
import com.diligrp.upay.trade.service.IPaymentPlatformService;
import jakarta.annotation.Resource;

/**
 * 交易服务组件
 */
@CallableComponent(id = "payment.trade.service")
public class TradeServiceComponent {

    @Resource
    private IPaymentPlatformService paymentPlatformService;

    /**
     * 交易订单提交支付，适用于所有交易业务
     * 预授权交易提交支付时只是冻结资金，后续需要进一步调用confirm/cancel进行资金操作
     */
    public PaymentResult commit(ServiceRequest<TradeDTO> request) {
        TradeDTO trade = request.getData();
        // 基本参数校验
        AssertUtils.notNull(trade.getType(), "type missed");
        AssertUtils.notNull(trade.getAccountId(), "accountId missed");
        AssertUtils.notNull(trade.getAmount(), "amount missed");
        AssertUtils.isTrue(trade.getAmount() > 0, "Invalid amount");
        AssertUtils.notEmpty(trade.getOutTradeNo(), "outTradeNo missed");

        AssertUtils.notNull(trade.getPayment(), "payment missed");
        PaymentDTO payment = trade.getPayment();
        AssertUtils.notNull(payment.getAccountId(), "payment.accountId missed");
        AssertUtils.notNull(payment.getChannelId(), "channelId missed");
        // 费用参数校验
        payment.fees().ifPresent(fees -> fees.forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        return paymentPlatformService.commit(permit, trade);
    }

    /**
     * 确认交易，只适用于预授权交易
     * 预授权交易需经历 commit->confirm/cancel两个阶段
     * confirm阶段解冻资金并完成实际交易消费，实际交易金额可以大于冻结金额（原订单金额）
     */
    public PaymentResult confirm(ServiceRequest<ConfirmDTO> request) {
        ConfirmDTO confirm = request.getData();
        AssertUtils.notEmpty(confirm.getTradeId(), "tradeId missed");
        AssertUtils.notNull(confirm.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(confirm.getPassword(), "password missed");
        AssertUtils.notNull(confirm.getAmount(), "amount missed");
        AssertUtils.isTrue(confirm.getAmount() > 0, "Invalid amount");
        // 费用参数校验
        confirm.fees().ifPresent(fees -> fees.forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        return paymentPlatformService.confirm(permit, confirm);
    }

    /**
     * 交易退款
     */
    public PaymentResult refund(ServiceRequest<RefundDTO> request) {
        RefundDTO refund = request.getData();
        AssertUtils.notEmpty(refund.getTradeId(), "tradeId missed");
        // 退款金额有效性检查放在各服务内判断
        AssertUtils.notNull(refund.getAmount(), "amount missed");
        // 费用参数校验
        refund.fees().ifPresent(fees -> fees.forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);
        return paymentPlatformService.refund(application, refund);
    }

    /**
     * 取消交易，适用于普通交易和预授权交易类型
     * 预授权交易需经历 commit->confirm/cancel两个阶段
     * 预授权交易的cancel阶段完成资金解冻，不进行任何消费；交易确认后cancel将完成资金逆向操作
     */
    public PaymentResult cancel(ServiceRequest<RefundDTO> request) {
        RefundDTO cancel = request.getData();
        AssertUtils.notEmpty(cancel.getTradeId(), "tradeId missed");

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);
        return paymentPlatformService.cancel(application, cancel);
    }

    /**
     * 交易冲正, 只有充值和提现才允许交易冲正
     */
    public PaymentResult correct(ServiceRequest<CorrectDTO> request) {
        CorrectDTO correct = request.getData();
        AssertUtils.notEmpty(correct.getTradeId(), "tradeId missed");
        AssertUtils.notNull(correct.getAccountId(), "accountId missed");
        // 冲正金额有效性检查放在各服务内判断，充值和提现冲正金额有效性校验不同
        AssertUtils.notNull(correct.getAmount(), "amount missed");

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class);
        return paymentPlatformService.correct(application, correct);
    }
}