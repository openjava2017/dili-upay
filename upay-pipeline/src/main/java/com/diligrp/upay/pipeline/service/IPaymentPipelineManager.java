package com.diligrp.upay.pipeline.service;

import com.diligrp.upay.pipeline.core.PaymentPipeline;

import java.util.List;

/**
 * 支付通道管理器接口
 */
public interface IPaymentPipelineManager {
    /**
     * 注册支付通道
     */
    <T extends PaymentPipeline> void registerPaymentPipeline(T pipeline);

    /**
     * 获取商户配置的指定类型的支付通道
     */
    <T extends PaymentPipeline> T findPipelineByMchId(long mchId, Class<T> type);

    /**
     * 获取商户配置的指定类型的支付通道
     */
    <T extends PaymentPipeline> List<T> listPipelines(long mchId, Class<T> type);

    /**
     * 根据ID获取支付通道
     */
    <T extends PaymentPipeline> T findPipelineById(long pipelineId, Class<T> type);
}
