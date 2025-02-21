package com.diligrp.upay.core.util;

@FunctionalInterface
public interface Configurer<T> {
    void configure(T t);

    static <T> Configurer<T> withDefaults() {
        return (t) -> {
        };
    }
}
