package com.diligrp.upay.core;

public final class Constants {
    // 商户偏好设置REDIS KEY
    public static final String PREFERENCE_REDIS_KEY = "upay:merchant:%s:preference";
    // 数据字典全局组名-适用所有商户的配置组，字典值分组的目的是处理各商户有独立的配置信息
    public static final String GLOBAL_CFG_GROUP = "GlobalSysCfg";
    // 数据字典常量-接口数据签名配置参数
    public static final String CONFIG_DATA_SIGN = "dataSignSwitch";
    // 数据字典常量-最大免密支付金额
    public static final String CONFIG_MAX_PROTO_AMOUNT = "maxProtocolAmount";
    // 默认最大免密支付金额 - 100元
    public static final long DEFAULT_MAX_PROTO_AMOUNT = 10000L;
    // 数据字典常量-参数值: 开关打开
    public static final String SWITCH_ON = "on";
}
