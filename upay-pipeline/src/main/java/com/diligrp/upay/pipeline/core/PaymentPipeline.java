package com.diligrp.upay.pipeline.core;

import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.shared.util.ObjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class PaymentPipeline<T extends PaymentPipeline.PipelineParams> implements IPipeline<T> {
    // 通道所属商户
    private final long mchId;
    // 通道ID
    private final long pipelineId;
    // 通道名称
    private final String name;
    // 通道服务URI
    private final String uri;
    // 通道参数配置
    private final T params;

    public PaymentPipeline(long mchId, long pipelineId, String name, String uri, String params) throws Exception {
        this.mchId = mchId;
        this.pipelineId = pipelineId;
        this.name = name;
        this.uri = uri;

        Constructor<T> constructor = paramClass().getConstructor(String.class);
        try {
            this.params = constructor.newInstance(params);
        } catch (InvocationTargetException tex) {
            throw (Exception) tex.getCause();
        }
    }

    @Override
    public long mchId() {
        return this.mchId;
    }

    @Override
    public long pipelineId() {
        return this.pipelineId;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String uri() {
        return this.uri;
    }

    @Override
    public T params() {
        return params;
    }

    protected void checkParam(String label, String value) {
        if (ObjectUtils.isEmpty(value)) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, String.format("支付通道缺少参数配置: %s", label));
        }
    }

    protected static abstract class PipelineParams {
        public PipelineParams(String params) {
            parseParams(params);
        }

        protected void parseParams(String params) {
            JsonUtils.fromJsonString(this, params);
        }
    }

    public static class NoneParams extends PipelineParams {
        public NoneParams(String params) {
            super(params);
        }

        @Override
        protected void parseParams(String params) {
        }
    }
}
