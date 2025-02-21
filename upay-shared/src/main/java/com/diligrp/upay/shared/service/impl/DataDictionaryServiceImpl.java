package com.diligrp.upay.shared.service.impl;

import com.diligrp.upay.shared.Constants;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.dao.DataDictionaryDao;
import com.diligrp.upay.shared.domain.Dictionary;
import com.diligrp.upay.shared.exception.PlatformServiceException;
import com.diligrp.upay.shared.model.DataDictionary;
import com.diligrp.upay.shared.redis.LettuceTemplate;
import com.diligrp.upay.shared.service.DataDictionaryService;
import com.diligrp.upay.shared.util.AssertUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("dataDictionaryService")
public class DataDictionaryServiceImpl implements DataDictionaryService {

    private static final Logger LOG = LoggerFactory.getLogger(DataDictionaryServiceImpl.class);

    private static final int DICTIONARY_EXPIRE_TIME = 2 * 60 * 60; // 字典缓存过期时间

    @Resource
    private DataDictionaryDao dataDictionaryDao;

    @Resource
    private LettuceTemplate<String, String> lettuceTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDataDictionary(Dictionary dictionary) {
        LocalDateTime when = LocalDateTime.now();
        DataDictionary dataDictionary = DataDictionary.builder().groupCode(dictionary.getGroupCode())
            .code(dictionary.getCode()).name(dictionary.getName()).value(dictionary.getValue())
            .description(dictionary.getDescription()).createdTime(when).modifiedTime(when).build();
        dataDictionaryDao.insertDataDictionary(dataDictionary);
    }

    @Override
    public String loadDictionaryValue(String groupCode, String code) {
        AssertUtils.notEmpty(code, "code missed");
        AssertUtils.notEmpty(groupCode, "groupCode missed");

        String cachedKey = String.format(Constants.DICTIONARY_REDIS_KEY, groupCode, code);;
        String cachedValue = loadCachedDictionary(cachedKey);

        if (Objects.isNull(cachedValue)) {
            Optional<DataDictionary> dictionaryOpt = dataDictionaryDao.findDataDictionaryByCode(groupCode, code);
            if (dictionaryOpt.isPresent()) {
                cachedValue = dictionaryOpt.get().getValue();
                saveCachedDictionary(cachedKey, cachedValue);
            }
        }

        return cachedValue;
    }

    @Override
    public List<DataDictionary> findDataDictionaries(String groupCode) {
        return dataDictionaryDao.findDataDictionaries(groupCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataDictionary(DataDictionary dictionary) {
        String cachedKey = String.format(Constants.DICTIONARY_REDIS_KEY, dictionary.getGroupCode(), dictionary.getCode());
        clearCachedDictionary(cachedKey);

        int result = dataDictionaryDao.updateDataDictionary(dictionary);
        if (result == 0) {
            throw new PlatformServiceException(ErrorCode.OBJECT_NOT_FOUND, "数据字典不存在");
        }
    }

    /**
     * Redis加载缓存的数据字典配置，程序异常时返回NULL
     */
    private String loadCachedDictionary(String cachedKey) {
        try {
            return lettuceTemplate.getAndExpire(cachedKey, DICTIONARY_EXPIRE_TIME);
        } catch (Exception ex) {
            LOG.error("Failed to load cached data dictionary", ex);
        }

        return null;
    }

    /**
     * Redis缓存存储数据字典配置，忽略程序异常
     */
    private void saveCachedDictionary(String cachedKey, String cachedValue) {
        try {
            lettuceTemplate.setAndExpire(cachedKey, cachedValue, DICTIONARY_EXPIRE_TIME);
        } catch (Exception ex) {
            LOG.error("Failed to save cached data dictionary", ex);
        }
    }

    /**
     * 清理Redis数据字典缓存，忽略程序异常
     */
    private void clearCachedDictionary(String cachedKey) {
        try {
            lettuceTemplate.del(cachedKey);
        } catch (Exception ex) {
            LOG.error("Failed to clear cached data dictionary", ex);
        }
    }
}
