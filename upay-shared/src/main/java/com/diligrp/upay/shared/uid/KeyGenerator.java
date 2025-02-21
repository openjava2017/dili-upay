package com.diligrp.upay.shared.uid;

/**
 * SequenceKey基础类
 */
public interface KeyGenerator {
    /**
     * 获取下一个ID
     *
     * @return 下一个ID
     */
    String nextId();
}
