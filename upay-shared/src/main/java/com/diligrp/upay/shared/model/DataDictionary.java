package com.diligrp.upay.shared.model;

import java.time.LocalDateTime;

public class DataDictionary extends BaseDO {
    // 分组编码
    private String groupCode;
    // 编码
    private String code;
    // 名称
    private String name;
    // 字典值
    private String value;
    // 描述
    private String description;

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public static Builder builder() {
        return new DataDictionary().new Builder();
    }

    public class Builder {
        public Builder groupCode(String groupCode) {
            DataDictionary.this.groupCode = groupCode;
            return this;
        }

        public Builder code(String code) {
            DataDictionary.this.code = code;
            return this;
        }

        public Builder name(String name) {
            DataDictionary.this.name = name;
            return this;
        }

        public Builder value(String value) {
            DataDictionary.this.value = value;
            return this;
        }

        public Builder description(String description) {
            DataDictionary.this.description = description;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            DataDictionary.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            DataDictionary.this.modifiedTime = modifiedTime;
            return this;
        }

        public DataDictionary build() {
            return DataDictionary.this;
        }
    }
}
