package com.diligrp.upay.pipeline.service.impl;

import com.diligrp.upay.pipeline.core.IPipeline;
import com.diligrp.upay.pipeline.core.PaymentPipeline;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.pipeline.dao.IPaymentPipelineDao;
import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.pipeline.impl.WechatDirectPipeline;
import com.diligrp.upay.pipeline.impl.WechatPartnerPipeline;
import com.diligrp.upay.pipeline.model.WechatParam;
import com.diligrp.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.upay.pipeline.type.PipelineType;
import com.diligrp.upay.pipeline.type.WechatPipelineType;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.service.LifeCycle;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("paymentPipelineManager")
public class PaymentPipelineManagerImpl extends LifeCycle implements IPaymentPipelineManager {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentPipelineManagerImpl.class);

    private final Map<Long, IPipeline> pipelines = new HashMap<>();

    @Resource
    private IPaymentPipelineDao paymentPipelineDao;

    protected void doStart() throws Exception {
        List<com.diligrp.upay.pipeline.model.PaymentPipeline> pipelines = paymentPipelineDao.listPaymentPipelines();
        for (com.diligrp.upay.pipeline.model.PaymentPipeline pipeline : pipelines) {
            if (PipelineType.WECHAT_PAY.equalTo(pipeline.getType())) {
                Optional<WechatParam> paramOpt = paymentPipelineDao.findWechatParam(pipeline.getPipelineId());
                if (paramOpt.isPresent()) {
                    WechatParam param = paramOpt.get();
                    if (WechatPipelineType.DIRECT.equalTo(param.getType())) {
                        WechatPipeline paymentPipeline = new WechatDirectPipeline(pipeline.getMchId(), pipeline.getPipelineId(),
                            pipeline.getName(), pipeline.getUri(), pipeline.getParam());
                        paymentPipeline.configure(param.getMchId(), param.getAppId(), param.getAppSecret(), param.getSerialNo(),
                            param.getPrivateKey(), param.getWechatSerialNo(), param.getWechatPublicKey(), param.getApiV3Key());
                        registerPaymentPipeline(paymentPipeline);
                    } else if (WechatPipelineType.PARTNER.equalTo(param.getType())) {
                        WechatPipeline paymentPipeline = new WechatPartnerPipeline(pipeline.getMchId(), pipeline.getPipelineId(),
                            pipeline.getName(), pipeline.getUri(), pipeline.getParam());
                        paymentPipeline.configure(param.getMchId(), param.getAppId(), param.getAppSecret(), param.getSerialNo(),
                            param.getPrivateKey(), param.getWechatSerialNo(), param.getWechatPublicKey(), param.getApiV3Key());
                        registerPaymentPipeline(paymentPipeline);
                    } else {
                        LOG.warn("Ignore wechat payment pipeline: {}, because of invalid wechat pipeline type", pipeline.getName());
                    }
                } else {
                    LOG.warn("Ignore wechat payment pipeline: {}, since wechat params not found", pipeline.getName());
                }
            }
        }
    }

    @Override
    public <T extends PaymentPipeline> void registerPaymentPipeline(T pipeline) {
        pipelines.put(pipeline.pipelineId(), pipeline);
        LOG.info("{} payment pipeline registered", pipeline.name());
    }

    @Override
    public <T extends PaymentPipeline> T findPipelineByMchId(long mchId, Class<T> type) {
        List<T> allPipelines = pipelines.values().stream().filter(p -> p.mchId() == mchId)
            .filter(p -> type.isAssignableFrom(p.getClass())).map(p -> type.cast(p)).collect(Collectors.toList());
        if (pipelines.isEmpty()) {
            throw new PaymentPipelineException(ErrorCode.OBJECT_NOT_FOUND, "该商户未配置此支付通道");
        }
        if (pipelines.size() > 1) {
            throw new PaymentPipelineException(ErrorCode.OBJECT_NOT_FOUND, "该商户未配置支付路由");
        }
        return allPipelines.get(0);
    }

    @Override
    public <T extends PaymentPipeline> List<T> listPipelines(long mchId, Class<T> type) {
        return pipelines.values().stream().filter(p -> p.mchId() == mchId)
            .filter(p -> type.isAssignableFrom(p.getClass())).map(p -> type.cast(p)).collect(Collectors.toList());
    }

    @Override
    public <T extends PaymentPipeline> T findPipelineById(long pipelineId, Class<T> type) {
        return pipelines.values().stream().filter(p -> p.pipelineId() == pipelineId && type.isAssignableFrom(p.getClass()))
            .map(p -> (T)p).findAny().orElseThrow(() -> new PaymentPipelineException(ErrorCode.OBJECT_NOT_FOUND, "系统不支持该支付通道"));
    }
}
