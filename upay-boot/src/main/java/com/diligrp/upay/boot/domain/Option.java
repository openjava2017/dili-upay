package com.diligrp.upay.boot.domain;

/**
 * Key-Value通用模型
 */
public class Option {
    // 编码
    private Integer code;
    // 名称
    private String name;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Option of(int code, String name) {
        Option option = new Option();
        option.setCode(code);
        option.setName(name);
        return option;
    }
}
