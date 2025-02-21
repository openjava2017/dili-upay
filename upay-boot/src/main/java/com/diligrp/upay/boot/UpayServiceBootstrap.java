package com.diligrp.upay.boot;

import com.diligrp.upay.core.CoreConfiguration;
import com.diligrp.upay.pipeline.PipelineConfiguration;
import com.diligrp.upay.sentinel.SentinelConfiguration;
import com.diligrp.upay.shared.SharedConfiguration;
import com.diligrp.upay.trade.TradeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({SharedConfiguration.class, CoreConfiguration.class, PipelineConfiguration.class, SentinelConfiguration.class,
        TradeConfiguration.class, BootConfiguration.class})
public class UpayServiceBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(UpayServiceBootstrap.class, args);
    }
}
