package com.diligrp.upay.shared;

import com.diligrp.upay.shared.codec.LettuceCodecs;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.shared.redis.LettuceConnectionFactory;
import com.diligrp.upay.shared.redis.LettuceProperties;
import com.diligrp.upay.shared.redis.LettuceTemplate;
import com.diligrp.upay.shared.util.JsonUtils;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.RedisClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
@ComponentScan("com.diligrp.upay.shared")
@MapperScan(basePackages =  {"com.diligrp.upay.shared.dao"}, markerInterface = MybatisMapperSupport.class)
public class SharedConfiguration {

    // Redis客户端Lettuce配置
    @Bean
    @ConfigurationProperties("spring.redis.lettuce")
    public LettuceProperties lettuceProperties() {
        return new LettuceProperties();
    }

    @Bean
    @ConditionalOnClass(RedisClient.class)
    public LettuceConnectionFactory lettuceConnectionFactory(LettuceProperties properties) {
        return new LettuceConnectionFactory(properties.getUrl());
    }

    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public LettuceTemplate<String, String> lettuceTemplate(LettuceConnectionFactory factory) {
        LettuceTemplate<String, String> template = new LettuceTemplate(factory);
        template.setRedisCodec(LettuceCodecs.STR_STR_CODEC);
        return template;
    }

    // Jackson DataBinding所有配置
    @Bean
    @ConditionalOnClass(JavaTimeModule.class)
    public Jackson2ObjectMapperBuilderCustomizer customizeJacksonConfig() {
        return JsonUtils::initObjectMapperBuilder;
    }

    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        // 不能使用lambda表达式，否则导致springboot启动问题
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                try {
                    return StringUtils.hasText(source) ? LocalDateTime.parse(source, DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)) : null;
                } catch (Exception ex) {
                    throw new IllegalArgumentException(String.format("Error parse %s to LocalDateTime", source), ex);
                }
            }
        };
    }

    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        // 不能使用lambda表达式，否则导致springboot启动问题
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                try {
                    return StringUtils.hasText(source) ? LocalDate.parse(source, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)) : null;
                } catch (Exception ex) {
                    throw new IllegalArgumentException(String.format("Error parse %s to LocalDate", source), ex);
                }
            }
        };
    }

    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        // 不能使用lambda表达式，否则导致springboot启动问题
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                try {
                    return StringUtils.hasText(source) ? LocalTime.parse(source, DateTimeFormatter.ofPattern(Constants.TIME_FORMAT)) : null;
                } catch (Exception ex) {
                    throw new IllegalArgumentException(String.format("Error parse %s to LocalTime", source), ex);
                }
            }
        };
    }

    @Bean
    public Converter<String, Date> dateConverter() {
        // 不能使用lambda表达式，否则导致springboot启动问题
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                try {
                    return StringUtils.hasText(source) ? new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(source) : null;
                } catch (Exception ex) {
                    throw new IllegalArgumentException(String.format("Error parse %s to Date", source), ex);
                }
            }
        };
    }
}