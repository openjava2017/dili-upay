package com.diligrp.upay.core;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.diligrp.upay.core")
@MapperScan(basePackages =  {"com.diligrp.upay.core.dao"}, markerInterface = MybatisMapperSupport.class)
public class CoreConfiguration {
}
