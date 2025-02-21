package com.diligrp.upay.shared.model;

import java.time.LocalDate;

/**
 * KEY-ID生成器数据库模型
 */
public class PersistentSequenceKey {
    /**
     * ID主键
     */
    private Long id;
    /**
     * KEY标识
     */
    private String key;
    /**
     * 名称
     */
    private String name;
    /**
     * 起始值
     */
    private Long value;
    /**
     * 步长
     */
    private Integer step;
    /**
     * ID格式
     */
    private String pattern;
    /**
     * 有效日期
     */
    private LocalDate expiredOn;
    /**
     * 当前日期-循环ID生成器使用
     */
    private LocalDate today;
    /**
     * 数据版本
     */
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public LocalDate getExpiredOn() {
        return expiredOn;
    }

    public void setExpiredOn(LocalDate expiredOn) {
        this.expiredOn = expiredOn;
    }

    public LocalDate getToday() {
        return today;
    }

    public void setToday(LocalDate today) {
        this.today = today;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static Builder builder() {
        return new PersistentSequenceKey().new Builder();
    }

    public class Builder {
        public Builder key(String key) {
            PersistentSequenceKey.this.key = key;
            return this;
        }

        public Builder name(String name) {
            PersistentSequenceKey.this.name = name;
            return this;
        }

        public Builder value(Long value) {
            PersistentSequenceKey.this.value = value;
            return this;
        }

        public Builder step(Integer step) {
            PersistentSequenceKey.this.step = step;
            return this;
        }

        public Builder pattern(String pattern) {
            PersistentSequenceKey.this.pattern = pattern;
            return this;
        }

        public Builder expiredOn(LocalDate expiredOn) {
            PersistentSequenceKey.this.expiredOn = expiredOn;
            return this;
        }

        public Builder version(Integer version) {
            PersistentSequenceKey.this.version = version;
            return this;
        }

        public PersistentSequenceKey build() {
            return PersistentSequenceKey.this;
        }
    }
}
