package com.diligrp.upay.core.service;

import com.diligrp.upay.core.domain.Preference;

/**
 * 支付偏好设置服务
 */
public interface IPreferenceService {
    /**
     * 商户偏好设置，并更新缓存
     */
    void setPreferences(Long mchId, Preference preference);

    /**
     * 获取商户偏好，先缓存再数据库读取
     */
    Preference getPreferences(Long mchId);

    /**
     * 接口数据签名是否开启
     *
     * @return 是否开启数据签名
     */
    boolean dataSignSwitch();

    /**
     * 最大免密协议支付金额
     *
     * @param groupCode - 必填, 字典分组编码(市场编码)
     * @return 最大免密协议支付金额
     */
    long maxProtocolAmount(String groupCode, Integer protocolType);
}