package com.diligrp.upay.sentinel;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.diligrp.upay.sentinel")
@MapperScan(basePackages =  {"com.diligrp.upay.sentinel.dao"}, markerInterface = MybatisMapperSupport.class)
public class SentinelConfiguration {
}
