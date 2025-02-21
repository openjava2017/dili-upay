package com.diligrp.upay.shared.uid.pattern;

public abstract class Converter<T> {
    private Converter<T> next;

    public abstract String convert(T t);

    public Converter<T> getNext() {
        return next;
    }

    public void setNext(Converter<T> next) {
        this.next = next;
    }
}
