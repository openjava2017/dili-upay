package com.diligrp.upay.core.service.impl;

import com.diligrp.upay.core.Constants;
import com.diligrp.upay.core.dao.IMerchantDao;
import com.diligrp.upay.core.domain.Preference;
import com.diligrp.upay.core.exception.PaymentServiceException;
import com.diligrp.upay.core.model.Merchant;
import com.diligrp.upay.core.service.IPreferenceService;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.redis.LettuceTemplate;
import com.diligrp.upay.shared.service.DataDictionaryService;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.shared.util.NumberUtils;
import com.diligrp.upay.shared.util.ObjectUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 支付偏好设置服务实现
 */
@Service("preferenceService")
public class PreferenceServiceImpl implements IPreferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceServiceImpl.class);

    private static final int CACHE_EXPIRE_TIME = 2 * 60 * 60; // 缓存过期时间

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private LettuceTemplate<String, String> lettuceTemplate;

    @Resource
    private DataDictionaryService dataDictionaryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPreferences(Long mchId, Preference preference) {
        LocalDateTime when = LocalDateTime.now();
        String payload = JsonUtils.toJsonString(preference);
        Merchant merchant = Merchant.builder().mchId(mchId).param(payload).modifiedTime(when).build();
        if (merchantDao.updateMerchant(merchant) == 0) {
            throw new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "商户偏好设置失败：该商户不存在");
        }

        try {
            // 缓存偏好设置
            String cachedKey = String.format(Constants.PREFERENCE_REDIS_KEY, mchId);
            lettuceTemplate.setAndExpire(cachedKey, payload, CACHE_EXPIRE_TIME);
        } catch (Exception ex) {
            LOG.error("Failed to set merchant preference cache", ex);
        }
    }

    @Override
    public Preference getPreferences(Long mchId) {
        Preference preference = new Preference();

        try {
            String cachedKey = String.format(Constants.PREFERENCE_REDIS_KEY, mchId);
            String payload = lettuceTemplate.getAndExpire(cachedKey, CACHE_EXPIRE_TIME);
            if (ObjectUtils.isEmpty(payload)) {
                Merchant merchant = merchantDao.findByMchId(mchId)
                    .orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "商户不存在"));
                payload = merchant.getParam() == null ? "{}" : merchant.getParam();
                lettuceTemplate.setAndExpire(cachedKey, payload, CACHE_EXPIRE_TIME);
            }

            preference = JsonUtils.fromJsonString(payload, Preference.class);
        } catch (Exception ex) {
            LOG.error("Failed to get merchant preference, use default one", ex);
        }

        preference.override(Preference.defaultPreference());
        return preference;
    }

    /**
     * {@inheritDoc}
     *
     * on-开启数据签名验签, off-关闭数据签名验签
     */
    @Override
    public boolean dataSignSwitch() {
        String dataSign = dataDictionaryService.loadDictionaryValue(Constants.GLOBAL_CFG_GROUP, Constants.CONFIG_DATA_SIGN);
        // 默认不开启接口签名
        return Constants.SWITCH_ON.equalsIgnoreCase(dataSign);
    }

    /**
     * {@inheritDoc}
     *
     * 获取最大免密支付金额
     */
    @Override
    public long maxProtocolAmount(String groupCode, Integer protocolType) {
        String maxAmount = dataDictionaryService.loadDictionaryValue(groupCode,
            Constants.CONFIG_MAX_PROTO_AMOUNT + protocolType);
        // 如无配置信息则使用全局配置金额100元 @see Constants.DEFAULT_MAX_PROTO_AMOUNT
        if (Objects.isNull(maxAmount)) {
            return Constants.DEFAULT_MAX_PROTO_AMOUNT;
        }

        if (!NumberUtils.isNumeric(maxAmount)) {
            throw new PaymentServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "免密协议参数配置错误");
        }
        return Long.parseLong(maxAmount);
    }
}
