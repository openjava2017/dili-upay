package com.diligrp.upay.pipeline.service.impl;

import com.diligrp.upay.shared.service.LifeCycle;
import org.springframework.stereotype.Component;

@Component("paymentPipelineConfigurer")
public class PaymentPipelineManagerImpl extends LifeCycle {
    protected void doStart() throws Exception {
        System.out.println("---------------------->");
    }
}
