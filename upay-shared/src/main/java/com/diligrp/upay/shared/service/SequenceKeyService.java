package com.diligrp.upay.shared.service;

import com.diligrp.upay.shared.model.PersistentSequenceKey;

/**
 * SequenceKey数据同步基础类
 *
 * @author: brenthuang
 * @date: 2020/03/24
 */
public interface SequenceKeyService {
    /**
     * 注册SequenceKey
     */
    void registerSequenceKey(PersistentSequenceKey sequenceKey);

    /**
     * 查找指定的SequenceKey
     *
     * @param key - SequenceKey的唯一标识
     * @return SequenceKey
     */
    PersistentSequenceKey findSequenceKey(String key);

    /**
     * 根据KeyId查询SequenceKey
     *
     * @param id - KeyId
     * @return SequenceKey
     */
    PersistentSequenceKey findSequenceKeyById(Long id);

    /**
     * 通过悲观锁实现同步从数据库获取基于过期日期的SequenceKey
     *
     * 根据数据库主键锁定数据记录(加行锁)，根据SequenceKey的过期日期更新下一个startWith值
     * 当SequenceKey过期时value将重新设置为1，否则value + 1
     *
     * @param id - KeyId
     * @return SequenceKey
     */
    PersistentSequenceKey synchronizeSequenceKey(Long id);
}
