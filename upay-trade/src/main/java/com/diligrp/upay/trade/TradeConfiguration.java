package com.diligrp.upay.trade;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.diligrp.upay.trade")
@MapperScan(basePackages =  {"com.diligrp.upay.trade.dao"}, markerInterface = MybatisMapperSupport.class)
public class TradeConfiguration {
}
