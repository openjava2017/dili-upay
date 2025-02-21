package com.diligrp.upay.sentinel.domain;

import com.diligrp.upay.sentinel.service.ISentinelAssistant;

/**
 * 风控哨兵模型
 */
public abstract class Sentinel {
    protected ISentinelAssistant sentinelAssistant;

    abstract void checkPassport(Passport passport);

    abstract void admitPassport(Passport passport);

    public void setSentinelAssistant(ISentinelAssistant sentinelAssistant) {
        this.sentinelAssistant = sentinelAssistant;
    }
}
