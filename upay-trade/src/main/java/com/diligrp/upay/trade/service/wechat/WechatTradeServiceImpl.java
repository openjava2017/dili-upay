package com.diligrp.upay.trade.service.wechat;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.pipeline.domain.WechatPaymentResponse;
import com.diligrp.upay.pipeline.domain.WechatPrepayResponse;
import com.diligrp.upay.pipeline.domain.WechatRefundResponse;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.trade.domain.wechat.WechatPrepayDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundDTO;
import com.diligrp.upay.trade.domain.wechat.WechatRefundResult;
import com.diligrp.upay.trade.service.IWechatTradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("wechatTradeService")
public class WechatTradeServiceImpl implements IWechatTradeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatPrepayResponse prepay(ApplicationPermit application, WechatPrepayDTO request) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyPaymentResult(WechatPaymentResponse response) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePrepayOrder(WechatPayment payment) {

    }
}
