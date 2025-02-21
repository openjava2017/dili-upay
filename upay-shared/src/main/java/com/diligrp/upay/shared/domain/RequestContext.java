package com.diligrp.upay.shared.domain;

/**
 * 请求上下文模型
 */
public class RequestContext extends ContainerSupport {

    private static final String PARAM_SERVICE = "service";

    public String getService() {
        return getString(PARAM_SERVICE);
    }
}