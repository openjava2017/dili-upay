package com.diligrp.upay.pipeline.dao;

import com.diligrp.upay.pipeline.domain.wechat.WechatPaymentDTO;
import com.diligrp.upay.pipeline.model.PaymentPipeline;
import com.diligrp.upay.pipeline.model.WechatPayment;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 微信支付通道数据访问层
 */
@Repository("wechatPaymentDao")
public interface IWechatPaymentDao extends MybatisMapperSupport {

    List<PaymentPipeline> insertWechatPayment(WechatPayment payment);

    Optional<WechatPayment> findByPaymentId(String paymentId);

    int compareAndSetState(WechatPaymentDTO paymentDTO);
}
