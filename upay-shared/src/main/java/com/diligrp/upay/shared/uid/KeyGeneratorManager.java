package com.diligrp.upay.shared.uid;

import com.diligrp.upay.shared.model.PersistentSequenceKey;
import com.diligrp.upay.shared.domain.SequenceKey;
import com.diligrp.upay.shared.service.SequenceKeyService;
import com.diligrp.upay.shared.uid.pattern.PatternLayout;
import com.diligrp.upay.shared.util.AssertUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service("keyGeneratorManager")
public class KeyGeneratorManager {
    private final SequenceKeyService sequenceKeyService;

    private final Lock locker = new ReentrantLock();

    private final ConcurrentMap<String, KeyGenerator> keyGenerators = new ConcurrentHashMap<>();

    public KeyGeneratorManager(SequenceKeyService sequenceKeyService) {
        this.sequenceKeyService = sequenceKeyService;
    }

    public KeyGenerator getKeyGenerator(String key) {
        AssertUtils.notNull(key, "Miss key parameter");

        KeyGenerator keyGenerator = keyGenerators.get(key);
        // First check, no need synchronize code block
        if (keyGenerator == null) {
            boolean result = false;
            try {
                result = locker.tryLock(15, TimeUnit.SECONDS);
                if (!result) {
                    throw new RuntimeException("Timeout to get KeyGenerator for " + key);
                }

                // Double check for performance purpose
                if ((keyGenerator = keyGenerators.get(key)) == null) {
                    PersistentSequenceKey persistentKey = sequenceKeyService.findSequenceKey(key);
                    if (persistentKey.getExpiredOn() == null) {
                        keyGenerator = new KeyGeneratorImpl(persistentKey.getId(), key, persistentKey.getPattern());
                    } else {
                        keyGenerator = new ExpiredKeyGeneratorImpl(persistentKey.getId(), key, persistentKey.getPattern());
                    }
                    keyGenerators.put(key, keyGenerator);
                }
            } catch (InterruptedException iex) {
                throw new RuntimeException("Interrupted to get KeyGenerator for " + key, iex);
            } finally {
                if (result) {
                    locker.unlock();
                }
            }
        }

        return keyGenerator;
    }

    private class KeyGeneratorImpl implements KeyGenerator {
        private final long id;
        private final String key;
        private final PatternLayout layout;
        private long startWith;
        private long endWith;
        private final Lock keyLocker = new ReentrantLock();

        public KeyGeneratorImpl(long id, String key, String pattern) {
            this.id = id;
            this.key = key;
            this.startWith = 0;
            this.endWith = -1;
            if (pattern != null) {
                this.layout = new PatternLayout(pattern);
            } else {
                this.layout = null;
            }
        }

        @Override
        public String nextId() {
            boolean result = false;
            try {
                result = keyLocker.tryLock(15L, TimeUnit.SECONDS);
                if (!result) {
                    throw new RuntimeException("Timeout to get KeyGenerator for " + key);
                }

                if (this.startWith <= this.endWith) {
                    if (this.layout != null) {
                        SequenceKey context = new SequenceKey(this.startWith++, LocalDate.now());
                        return layout.doLayout(context);
                    } else {
                        return String.valueOf(this.startWith++);
                    }
                } else {
                    PersistentSequenceKey sequenceKey = sequenceKeyService.synchronizeSequenceKey(id);
                    long newValue = sequenceKey.getValue() + sequenceKey.getStep();
                    this.startWith = sequenceKey.getValue();
                    this.endWith = newValue - 1;

                    // Then recursive call for a next ID
                    return nextId();
                }
            } catch (InterruptedException iex) {
                throw new RuntimeException("Interrupted to get KeyGenerator for " + key, iex);
            } finally {
                if (result) {
                    keyLocker.unlock();
                }
            }
        }
    }

    private class ExpiredKeyGeneratorImpl implements KeyGenerator {
        private final long id;
        private final String key;
        private final PatternLayout layout;

        private ExpiredKeyGeneratorImpl(long id, String key, String pattern) {
            this.id = id;
            this.key = key;
            if (pattern != null) {
                this.layout = new PatternLayout(pattern);
            } else {
                this.layout = null;
            }
        }

        @Override
        public String nextId() {
            //悲观锁添加行锁 - 多JVM多线程场景下自动实现线程同步
            PersistentSequenceKey persistentKey = sequenceKeyService.synchronizeSequenceKey(id);
            if (persistentKey == null) {
                throw new RuntimeException("Unregistered service key generator: " + key);
            }

            if (this.layout != null) {
                SequenceKey context = new SequenceKey(persistentKey.getValue(), persistentKey.getToday());
                return layout.doLayout(context);
            } else {
                return String.valueOf(persistentKey.getValue());
            }
        }
    }
}
