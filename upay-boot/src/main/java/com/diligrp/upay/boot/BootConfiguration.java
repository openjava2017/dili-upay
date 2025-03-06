package com.diligrp.upay.boot;

import com.diligrp.upay.shared.sapi.DefaultCallableServiceEngine;
import com.diligrp.upay.shared.sapi.DefaultCallableServiceManager;
import com.diligrp.upay.shared.sapi.ICallableServiceEngine;
import com.diligrp.upay.shared.sapi.ICallableServiceManager;
import com.diligrp.upay.shared.service.LifeCycle;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan("com.diligrp.upay.boot")
public class BootConfiguration implements ApplicationContextAware, ApplicationRunner {

    private ApplicationContext applicationContext;

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, LifeCycle> beans = applicationContext.getBeansOfType(LifeCycle.class);
        for (LifeCycle lifeCycle : beans.values()) {
            lifeCycle.start();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
