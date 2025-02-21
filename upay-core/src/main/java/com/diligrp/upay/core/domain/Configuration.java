package com.diligrp.upay.core.domain;

/**
 * 资金事务配置
 */
public class Configuration {

    public static Builder builder() {
        return new Configuration().new Builder();
    }

    public class Builder {

        public Configuration build() {
            return Configuration.this;
        }
    }
}
