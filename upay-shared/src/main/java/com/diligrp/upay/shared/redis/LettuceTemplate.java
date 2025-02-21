package com.diligrp.upay.shared.redis;

import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.time.Duration;

public class LettuceTemplate<K, V> implements InitializingBean, DisposableBean {
    private RedisCodec<K, V> redisCodec;
    private final LettuceConnectionFactory connectionFactory;
    private StatefulRedisConnection<K, V> connection;

    public LettuceTemplate(LettuceConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void set(K key, V value) {
        RedisCommands<K, V> command = connection.sync();
        command.set(key, value);
    }

    public void setAndExpire(K key, V value, int expireInSeconds) {
        RedisCommands<K, V> command = connection.sync();
        command.multi();
        command.set(key, value);
        command.expire(key, Duration.ofSeconds(expireInSeconds));
        command.exec();
    }

    public V get(K key) {
        RedisCommands<K, V> command = connection.sync();
        return command.get(key);
    }

    public V getAndExpire(K key, int expireInSeconds) {
        RedisCommands<K, V> command = connection.sync();
        command.multi();
        command.get(key);
        command.expire(key, Duration.ofSeconds(expireInSeconds));
        TransactionResult result = command.exec();
        return result.get(0);
    }

    public void hset(K key, K key1, V value) {
        RedisCommands<K, V> command = connection.sync();
        command.hset(key, key1, value);
    }

    public V hget(K key, K key1) {
        RedisCommands<K, V> command = connection.sync();
        return command.hget(key, key1);
    }

    public void expire(K key, int expireInSeconds) {
        RedisCommands<K, V> command = connection.sync();
        command.expire(key, Duration.ofSeconds(expireInSeconds));
    }

    public long incrAndGet(K key, int expireInSeconds) {
        RedisCommands<K, V> command = connection.sync();
        command.multi();
        command.incr(key);
        command.expire(key, Duration.ofSeconds(expireInSeconds));
        TransactionResult result = command.exec();
        return result.get(0);
    }

    public long del(K... key) {
        RedisCommands<K, V> command = connection.sync();
        return command.del(key);
    }

    public void hdel(K key, K... keys) {
        RedisCommands<K, V> command = connection.sync();
        command.hdel(key, keys);
    }

    public void execute(TransactionCallback<K, V> callback) {
        RedisCommands<K, V> command = connection.sync();
        command.multi();
        callback.doInTransaction(command);
    }

    public interface TransactionCallback<K, V> {
        void doInTransaction(RedisCommands<K, V> command);
    }

    public void setRedisCodec(RedisCodec<K, V> redisCodec) {
        this.redisCodec = redisCodec;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(redisCodec, "redisCodec must be specified");
        this.connection = this.connectionFactory.getConnection(redisCodec);
        // 多线程共用一个连接时，需设置自动提交命令，默认值为true
        this.connection.setAutoFlushCommands(true);
    }

    @Override
    public void destroy() {
        this.connection.close();
    }
}
