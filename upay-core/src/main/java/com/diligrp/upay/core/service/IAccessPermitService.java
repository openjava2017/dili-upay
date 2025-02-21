package com.diligrp.upay.core.service;

import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.domain.RegisterApplication;
import com.diligrp.upay.core.domain.RegisterMerchant;

/**
 * 平台接入许可服务
 */
public interface IAccessPermitService {
    /**
     * 获取商户接入许可
     */
    MerchantPermit loadMerchantPermit(Long mchId);

    /**
     * 获取应用接入许可
     */
    ApplicationPermit loadApplicationPermit(Long appId);

    /**
     * 注册商户: 创建接入商户(分配mchId)、创建商户账户（收益账户、担保账户和押金账户等）并分配商户公私钥
     */
    MerchantPermit registerMerchant(RegisterMerchant request);

    /**
     * 修改商户信息: 修改商户名等信息
     */
    void modifyMerchant(RegisterMerchant request);

    /**
     * 注册应用: 创建商户应用(分配appId和accessToken)，分配商户应用公私钥
     */
    ApplicationPermit registerApplication(RegisterApplication request);
}
