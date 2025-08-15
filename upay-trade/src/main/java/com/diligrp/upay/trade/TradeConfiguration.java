package com.diligrp.upay.trade;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.diligrp.upay.trade")
@MapperScan(basePackages =  {"com.diligrp.upay.trade.dao"}, markerInterface = MybatisMapperSupport.class)
public class TradeConfiguration {
    /**
     * 支付通道MQ延时队列
     * 队列为持久化、非独占式且不自动删除的队列, 利用RabbitMQ延时插件实现延时功能https://github.com/rabbitmq/rabbitmq-delayed-message-exchange
     */
    @Bean
    public Queue paymentDelayQueue() {
        return new Queue(Constants.PAYMENT_DELAY_QUEUE, true, false, false);
    }

    /**
     * 支付通道MQ延时交换机
     */
    @Bean
    public CustomExchange paymentDelayExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(Constants.PAYMENT_DELAY_EXCHANGE, "x-delayed-message", true, false, arguments);
    }

    /**
     * 支付通道MQ延时队列和交换机的绑定
     */
    @Bean
    public Binding paymentDelayBinding() {
        return BindingBuilder.bind(paymentDelayQueue()).to(paymentDelayExchange()).with(Constants.PAYMENT_DELAY_KEY).noargs();
    }
}
