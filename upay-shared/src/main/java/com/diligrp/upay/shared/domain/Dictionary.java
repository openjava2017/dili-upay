package com.diligrp.upay.shared.domain;

public class Dictionary {
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

    public static Dictionary of(String groupCode, String code, String name, String value) {
        Dictionary dictionary = new Dictionary();
        dictionary.setGroupCode(groupCode);
        dictionary.setCode(code);
        dictionary.setName(name);
        dictionary.setValue(value);
        return dictionary;
    }

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
}
