package com.diligrp.upay.pipeline.dao;

import com.diligrp.upay.pipeline.model.PaymentPipeline;
import com.diligrp.upay.pipeline.model.WechatParam;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付通道数据访问层
 */
@Repository("paymentPipelineDao")
public interface IPaymentPipelineDao extends MybatisMapperSupport {

    List<PaymentPipeline> listPaymentPipelines();

    Optional<WechatParam> findWechatParam(Long pipelineId);
}
