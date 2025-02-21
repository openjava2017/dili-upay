package com.diligrp.upay.shared.dao;

import com.diligrp.upay.shared.model.PersistentSequenceKey;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * KeySequence数据操作
 */
@Repository("sequenceKeyDao")
public interface SequenceKeyDao extends MybatisMapperSupport {
    /**
     * 注册SequenceKey
     */
    void insertSequenceKey(PersistentSequenceKey sequenceKey);

    /**
     * 查询指定的SequenceKey
     *
     * @param key - SequenceKey唯一标识
     * @return SequenceKey
     */
    Optional<PersistentSequenceKey> findSequenceKey(String key);

    /**
     * 根据KeyId查询SequenceKey
     *
     * @param id - KeyId
     * @return SequenceKey
     */
    Optional<PersistentSequenceKey> findSequenceKeyById(Long id);

    /**
     * 悲观锁实现 - 根据数据库主键锁定数据记录
     *
     * @param id - KeyId
     * @return SequenceKey
     */
    Optional<PersistentSequenceKey> lockSequenceKey(Long id);

    /**
     * 悲观锁解锁实现 - 根据数据库主键解锁数据记录
     *
     * @param sequenceKey - 参数列表：id/newValue/expiredOn
     * @return 1-更新成功，0-更新失败
     */
    int unlockSequenceKey(PersistentSequenceKey sequenceKey);
}
