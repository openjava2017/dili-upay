package com.diligrp.upay.pipeline;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.diligrp.upay.pipeline")
@MapperScan(basePackages =  {"com.diligrp.upay.pipeline.dao"}, markerInterface = MybatisMapperSupport.class)
public class PipelineConfiguration {
}
