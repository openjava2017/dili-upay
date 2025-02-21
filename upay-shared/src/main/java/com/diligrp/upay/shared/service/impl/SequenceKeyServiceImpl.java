package com.diligrp.upay.shared.service.impl;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.dao.SequenceKeyDao;
import com.diligrp.upay.shared.model.PersistentSequenceKey;
import com.diligrp.upay.shared.exception.PlatformServiceException;
import com.diligrp.upay.shared.service.SequenceKeyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * SequenceKey数据同步的实现类
 */
@Service("sequenceKeyService")
public class SequenceKeyServiceImpl implements SequenceKeyService {

    private final SequenceKeyDao sequenceKeyDao;

    public SequenceKeyServiceImpl(SequenceKeyDao sequenceKeyDao) {
        this.sequenceKeyDao = sequenceKeyDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerSequenceKey(PersistentSequenceKey sequenceKey) {
        sequenceKeyDao.insertSequenceKey(sequenceKey);
    }

    @Override
    public PersistentSequenceKey findSequenceKey(String key) {
        return sequenceKeyDao.findSequenceKey(key).orElseThrow(() ->
            new PlatformServiceException(ErrorCode.OBJECT_NOT_FOUND, "没有配置该SequenceKey"));
    }

    @Override
    public PersistentSequenceKey findSequenceKeyById(Long id) {
        return sequenceKeyDao.findSequenceKeyById(id).orElseThrow(() ->
            new PlatformServiceException(ErrorCode.OBJECT_NOT_FOUND, "没有配置该SequenceKey"));
    }

    /**
     * 数据库的行锁只有在事务提交之后才会释放，这里使用业务层的Spring事务，因此行锁将不能很快释放，这样势必会降低此代码块的并发性能。
     * 如果新开一个Spring事务Propagation.REQUIRES_NEW，与业务层事务独立将无法保证ID的连续性（不能随着业务层的失败回滚生成的ID）
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public PersistentSequenceKey synchronizeSequenceKey(Long id) {
        // 悲观锁添加行锁 - 多JVM多线程场景下自动实现线程同步
        // 通过SELECT FOR UPDATE锁定了行，当事务提交时将自动释放行锁
        PersistentSequenceKey persistentKey = sequenceKeyDao.lockSequenceKey(id).orElseThrow(() ->
            new PlatformServiceException(ErrorCode.OBJECT_NOT_FOUND, "没有配置该SequenceKey"));

        // 当PersistentKey设置了过期时间并且已经过期时, 则value重新设置成1并刷新过期日期为今天，否则设置value+=step
        LocalDate today = persistentKey.getToday();
        LocalDate expiredDay = persistentKey.getExpiredOn();
        if (expiredDay != null && today.isAfter(persistentKey.getExpiredOn())) {
            persistentKey.setValue(1L);
            expiredDay = today;
        }

        PersistentSequenceKey newKey = new PersistentSequenceKey();
        newKey.setId(persistentKey.getId());
        newKey.setValue(persistentKey.getValue() + persistentKey.getStep());
        newKey.setExpiredOn(expiredDay);

        sequenceKeyDao.unlockSequenceKey(newKey);

        return persistentKey;
    }
}
