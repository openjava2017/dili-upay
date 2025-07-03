package com.diligrp.upay.trade.message;

import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageConsumeService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageConsumeService.class);

    @Resource
    private IWechatPaymentService wechatPaymentService;

    /**
     * 监听支付通道异步处理任务消息
     */
    @RabbitHandler
    @RabbitListener(queues = {MessageConstants.PIPELINE_DELAY_QUEUE})
    public void consumeMessage(Message message) {
        byte[] packet = message.getBody();
        MessageProperties properties = message.getMessageProperties();
        String charSet = properties != null && properties.getContentEncoding() != null
            ? properties.getContentEncoding() : StandardCharsets.UTF_8.name();
        try {
            String body = new String(packet, charSet);
            LOG.info("Receiving pipeline async task request: {}", body);
            TaskMessage task = JsonUtils.fromJsonString(body, TaskMessage.class);
            if (task.getType() == TaskMessage.TYPE_WECHAT_PREPAY_SCAN) { // 十分钟兜底处理微信预支付订单
                wechatPaymentService.scanWechatPrepayOrder(task.getPayload());
            } else if (task.getType() == TaskMessage.TYPE_WECHAT_REFUND_SCAN) { // 十分钟兜底处理微信退款订单
                wechatPaymentService.scanWechatRefundOrder(task.getPayload());
            }
        } catch (Exception ex) {
            LOG.error("Consume pipeline async message exception", ex);
        }
    }
}
