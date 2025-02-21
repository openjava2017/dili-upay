package com.diligrp.upay.boot;

import com.diligrp.upay.shared.sapi.DefaultCallableServiceEngine;
import com.diligrp.upay.shared.sapi.DefaultCallableServiceManager;
import com.diligrp.upay.shared.sapi.ICallableServiceEngine;
import com.diligrp.upay.shared.sapi.ICallableServiceManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.diligrp.upay.boot")
public class BootConfiguration {

    @Bean(name = "callableServiceEngine") // BeanPostProcessor需通过static方法创建
    @ConditionalOnMissingBean
    public static ICallableServiceEngine callableServiceEngine() {
        return new DefaultCallableServiceEngine();
    }

    @Bean(name = "callableServiceManager")
    @ConditionalOnMissingBean
    public ICallableServiceManager callableServiceManager(ICallableServiceEngine callableServiceEngine) {
        return new DefaultCallableServiceManager(callableServiceEngine);
    }
}
