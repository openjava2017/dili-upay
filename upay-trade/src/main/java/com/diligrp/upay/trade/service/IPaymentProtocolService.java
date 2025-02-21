package com.diligrp.upay.trade.service;

import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.trade.domain.ProtocolQuery;
import com.diligrp.upay.trade.domain.ProtocolDTO;
import com.diligrp.upay.trade.model.UserProtocol;

/**
 * 免密支付协议服务接口
 */
public interface IPaymentProtocolService {
    /**
     * 注册免密支付协议
     *
     * @param request - 注册申请
     * @return 用户协议
     */
    UserProtocol registerUserProtocol(ProtocolDTO request);

    /**
     * 查询免密支付协议
     *
     * @param request - 查询申请
     * @return 用户协议
     */
    UserProtocol queryUserProtocol(ProtocolQuery request);

    /**
     * 检查免密支付权限
     */
    void checkProtocolPermission(UserAccount account, long protocolId, long amount);
}
