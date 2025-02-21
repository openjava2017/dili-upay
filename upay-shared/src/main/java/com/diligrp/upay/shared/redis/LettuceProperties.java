package com.diligrp.upay.shared.redis;

public class LettuceProperties {

    // redis://username:password@localhost:port/database?timeout=15s
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}