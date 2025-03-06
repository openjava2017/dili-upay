package com.diligrp.upay.pipeline.core;

import com.diligrp.upay.pipeline.type.ChannelType;

/**
 * 支付通道领域模型接口
 */
public interface IPipeline<T> {
    /**
     * 通道所属商户
     */
    long mchId();

    /**
     * 通道ID
     */
    long pipelineId();

    /**
     * 通道名称
     */
    String name();

    /**
     * 通道服务URI
     */
    String uri();

    /**
     * 通道所属渠道
     */
    ChannelType supportedChannel();

    /**
     * 配置参数
     */
    T params();

    /**
     * 配置参数类
     */
    Class<T> paramClass();
}
