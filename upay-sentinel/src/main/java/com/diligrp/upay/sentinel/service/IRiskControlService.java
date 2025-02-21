package com.diligrp.upay.sentinel.service;

import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.sentinel.domain.PermissionDTO;
import com.diligrp.upay.sentinel.domain.RiskControlEngine;

/**
 * 风险控制服务接口
 */
public interface IRiskControlService {
    /**
     * 加载资金账号风控引擎
     */
    RiskControlEngine loadRiskControlEngine(UserAccount account);

    /**
     * 加载商户级权限和风控配置
     */
    PermissionDTO loadGlobalPermission(Long mchId);

    /**
     * 保存商户级权限和风控配置
     */
    void saveGlobalPermission(Long mchId, PermissionDTO permission);

    /**
     * 加载用户级权限和风控配置
     */
    PermissionDTO loadUserPermission(Long mchId, Long accountId);

    /**
     * 保存用户级权限和风控配置
     */
    void saveUserPermission(Long accountId, PermissionDTO permission);
}
