package com.diligrp.upay.trade.message;

import com.diligrp.upay.shared.service.ThreadPoolService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service("messageQueueService")
public class MessageQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueService.class);

    private static final long ONE_MINUTE = 60 * 1000;

    private static final long TEN_MINUTES = 10 * ONE_MINUTE;

    private static final long ONE_SECOND = 1000;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送微信订单扫描信息
     * 创建微信预支付订单成功后，通过MQ延时消息实现十分钟后根据微信支付查询结果，关闭或完成本地支付订单
     */
    public void sendWechatScanMessage(String paymentId) {
        ThreadPoolService.getIoThreadPoll().submit(() -> {
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentEncoding(StandardCharsets.UTF_8.name());
                properties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                // properties.setExpiration(String.valueOf(expiredTime));
                // RabbitMQ延时插件必须设置x-delay的header才能生效
                properties.setHeader("x-delay", String.valueOf(TEN_MINUTES));
                String body = TaskMessage.of(TaskMessage.TYPE_WECHAT_PREPAY_SCAN, paymentId).toString();
                org.springframework.amqp.core.Message message = new org.springframework.amqp.core.Message(body.getBytes(StandardCharsets.UTF_8), properties);
                LOG.info("Sending wechat pipeline order scan request for {}", paymentId);
                rabbitTemplate.send(MessageConstants.PIPELINE_DELAY_EXCHANGE, MessageConstants.PIPELINE_DELAY_KEY, message);
            } catch (Exception ex) {
                LOG.error(String.format("Failed to send wechat pipeline order scan request for %s", paymentId), ex);
            }
        });
    }
}
