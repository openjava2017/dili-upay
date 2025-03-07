package com.diligrp.upay.boot.component;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.exception.TradePaymentException;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import com.diligrp.upay.trade.type.TradeType;
import jakarta.annotation.Resource;

import java.util.Optional;

/**
 * 微信支付服务组件
 */
@CallableComponent(id = "wechat.payment.service")
public class WechatServiceComponent {

    @Resource
    private IWechatPaymentService wechatPaymentService;

    /**
     * 微信预支付
     */
    public WechatPrepayResponse prepare(ServiceRequest<WechatPrepayDTO> request) {
        WechatPrepayDTO prepayRequest = request.getData();
        Optional<TradeType> tradeType = TradeType.getType(prepayRequest.getType());
        tradeType.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的交易类型"));
        // 基本参数校验
        AssertUtils.notNull(prepayRequest.getPayType(), "payType missed");
        AssertUtils.notNull(prepayRequest.getAccountId(), "accountId missed");
        AssertUtils.notNull(prepayRequest.getGoods(), "goods missed");
        AssertUtils.notNull(prepayRequest.getAmount(), "amount missed");
        AssertUtils.isTrue(prepayRequest.getAmount() > 0, "Invalid amount");
        AssertUtils.notEmpty(prepayRequest.getOutTradeNo(), "outTradeNo missed");
        AssertUtils.notEmpty(prepayRequest.getNotifyUri(), "notifyUri missed");

        // 费用参数校验
        prepayRequest.fees().ifPresent(fees -> fees.stream().forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        return wechatPaymentService.prepay(permit, prepayRequest);
    }
}
