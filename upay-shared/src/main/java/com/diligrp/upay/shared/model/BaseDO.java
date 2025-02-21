package com.diligrp.upay.shared.model;

import java.time.LocalDateTime;

public class BaseDO {
    // 数据库主键
    protected Long id;
    // 数据版本
    protected Integer version;
    // 创建时间
    protected LocalDateTime createdTime;
    // 修改时间
    protected LocalDateTime modifiedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
}
