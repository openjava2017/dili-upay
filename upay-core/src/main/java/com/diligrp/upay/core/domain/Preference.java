package com.diligrp.upay.core.domain;

/**
 * 商户偏好设置，用于存储商户配置数据，比如：最大密码错误次数等
 */
public class Preference {
    // 最大密码错误次数
    private Integer maxPasswordErrors;
    // 是否开启短信通知
    private Boolean smsNotify;

    public Integer getMaxPasswordErrors() {
        return maxPasswordErrors;
    }

    public void setMaxPasswordErrors(Integer maxPasswordErrors) {
        this.maxPasswordErrors = maxPasswordErrors;
    }

    public boolean isSmsNotify() {
        return smsNotify;
    }

    public void setSmsNotify(boolean smsNotify) {
        this.smsNotify = smsNotify;
    }

    public static Preference defaultPreference() {
        return DEFAULT;
    }

    public void override(Preference preference) {
        if (preference.maxPasswordErrors != null) {
            this.maxPasswordErrors = preference.maxPasswordErrors;
        }
        if (preference.smsNotify != null) {
            this.smsNotify = preference.smsNotify;
        }
    }

    private static Preference DEFAULT;

    static {
        // 初始化默认偏好
        DEFAULT = new Preference();
        // 最大密码错误次数
        DEFAULT.maxPasswordErrors = 5;
        DEFAULT.smsNotify = Boolean.FALSE;
    }
}
