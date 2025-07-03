package com.diligrp.upay.trade.message;

import com.diligrp.upay.shared.service.ServiceEndpointSupport;
import com.diligrp.upay.shared.service.ThreadPoolService;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.trade.domain.wechat.WechatPaymentResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
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
    public void sendWechatScanMessage(TaskMessage task) {
        ThreadPoolService.getIoThreadPoll().submit(() -> {
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentEncoding(StandardCharsets.UTF_8.name());
                properties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                // properties.setExpiration(String.valueOf(expiredTime));
                // RabbitMQ延时插件必须设置x-delay的header才能生效
                properties.setHeader("x-delay", String.valueOf(TEN_MINUTES));
                String payload = JsonUtils.toJsonString(task);
                Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
                LOG.info("Sending wechat pipeline order scan request for {}", task.getPayload());
                rabbitTemplate.send(MessageConstants.PIPELINE_DELAY_EXCHANGE, MessageConstants.PIPELINE_DELAY_KEY, message);
            } catch (Exception ex) {
                LOG.error(String.format("Failed to send wechat pipeline order scan request for %s", task.getPayload()), ex);
            }
        });
    }

    /**
     * 通知业务系统微信支付通道处理结果
     */
    public void sendWechatNotifyMessage(String uri, Object payload) {
        ThreadPoolService.getIoThreadPoll().submit(() -> {
            try {
                String body = JsonUtils.toJsonString(payload);
                LOG.info("Notifying wechat pipeline payment result: {}", body);
                new NotifyHttpClient(uri).send(body);
            } catch (Exception ex) {
                LOG.error("Failed to notify wechat pipeline result", ex);
            }
        });
    }

    private class NotifyHttpClient extends ServiceEndpointSupport {
        private String baseUrl;

        public NotifyHttpClient(String baseUrl) {
            this.baseUrl =  baseUrl;
        }

        public HttpResult send(String body) {
            return send(baseUrl, body);
        }
    }
}
