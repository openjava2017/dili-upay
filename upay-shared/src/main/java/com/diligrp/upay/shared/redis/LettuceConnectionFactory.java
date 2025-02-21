package com.diligrp.upay.shared.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.Delay;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class LettuceConnectionFactory implements InitializingBean, DisposableBean {

    private final String uri;

    private RedisClient client;

    public LettuceConnectionFactory(String uri) {
        this.uri = uri;
    }

    public <K, V> StatefulRedisConnection<K, V> getConnection(RedisCodec<K, V> codec) {
        return this.client.connect(codec);
    }

    @Override
    public void afterPropertiesSet() {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ClientResources resources = DefaultClientResources.builder().ioThreadPoolSize(threads)
            .computationThreadPoolSize(threads).reconnectDelay(Delay::exponential).build();
        ClientOptions options = ClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true)
            .requestQueueSize(Integer.MAX_VALUE).timeoutOptions(TimeoutOptions.create()).build();
        this.client = RedisClient.create(resources, RedisURI.create(this.uri));
        this.client.setOptions(options);
    }

    @Override
    public void destroy() {
        this.client.close();
    }
}
