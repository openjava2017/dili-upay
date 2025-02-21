package com.diligrp.upay.sentinel.service.impl;

import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.sentinel.dao.IGlobalPermissionDao;
import com.diligrp.upay.sentinel.dao.IUserPermissionDao;
import com.diligrp.upay.sentinel.domain.*;
import com.diligrp.upay.sentinel.model.GlobalPermission;
import com.diligrp.upay.sentinel.model.UserPermission;
import com.diligrp.upay.sentinel.service.IRiskControlService;
import com.diligrp.upay.sentinel.service.ISentinelAssistant;
import com.diligrp.upay.shared.util.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 风险控制服务实现
 */
@Service("riskControlService")
public class RiskControlServiceImpl implements IRiskControlService {

    @Resource
    private IGlobalPermissionDao globalPermissionDao;

    @Resource
    private IUserPermissionDao userPermissionDao;

    @Resource
    private ISentinelAssistant executeAssistant;

    /**
     * {@inheritDoc}
     *
     * 用户风控配置参数为商户级风控配置和用户级风控配置的并集，当存在用户级配置参数则优先使用用户级参数，否则使用商户级配置参数;
     * 当两则都不存在时，则返回不受任何限制
     */
    @Override
    public RiskControlEngine loadRiskControlEngine(UserAccount account) {
        // 加载用户权限及风控配置
        PermissionDTO permission = loadUserPermission(account.getMchId(), account.getAccountId());

        // 构造和配置风控引擎
        RiskControlContext context = new RiskControlContext(executeAssistant);
        RiskControlEngine rc = new RiskControlEngine(context);
        rc.forPermission(permission.getPermission()).forDeposit(permission.getDeposit())
            .forWithdraw(permission.getWithdraw()).forTrade(permission.getTrade());
        return rc;
    }

    /**
     * {@inheritDoc}
     *
     * 如果未设置商户级权限和风控配置，则返回无任何限制的空配置
     */
    @Override
    public PermissionDTO loadGlobalPermission(Long mchId) {
        // 创建不受任何限制的空配置
        AtomicInteger permission = new AtomicInteger(Permission.ALL_PERMISSION);
        DepositPermission deposit = new DepositPermission();
        WithdrawPermission withdraw = new WithdrawPermission();
        TradePermission trade = new TradePermission();

        // 加载商户级风控配置
        Optional<GlobalPermission> globalOpt = globalPermissionDao.findByMchId(mchId);
        globalOpt.ifPresent(global -> {
            permission.set(global.getPermission());
            deposit.override(JsonUtils.fromJsonString(global.getDeposit(), DepositPermission.class));
            withdraw.override(JsonUtils.fromJsonString(global.getWithdraw(), WithdrawPermission.class));
            trade.override(JsonUtils.fromJsonString(global.getTrade(), TradePermission.class));
        });

        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPermission(permission.get());
        permissionDTO.setDeposit(deposit);
        permissionDTO.setWithdraw(withdraw);
        permissionDTO.setTrade(trade);
        return permissionDTO;
    }

    /**
     * {@inheritDoc}
     *
     * 如存在，则更新配置，否则新增配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGlobalPermission(Long mchId, PermissionDTO permission) {
        LocalDateTime now = LocalDateTime.now();
        // 默认空配置
        int bitmap = Permission.NO_PERMISSION;
        DepositPermission deposit = new DepositPermission();
        WithdrawPermission withdraw = new WithdrawPermission();
        TradePermission trade = new TradePermission();

        // 利用传入的配置参数进行参数覆盖
        if (permission.getPermission() != null) {
            bitmap = permission.getPermission();
        }
        deposit.override(permission.getDeposit());
        withdraw.override(permission.getWithdraw());
        trade.override(permission.getTrade());

        GlobalPermission request = GlobalPermission.builder().mchId(mchId).permission(bitmap)
            .deposit(JsonUtils.toJsonString(deposit)).withdraw(JsonUtils.toJsonString(withdraw))
            .trade(JsonUtils.toJsonString(trade)).createdTime(now).modifiedTime(now).build();
        int result = globalPermissionDao.updateGlobalPermission(request);
        if (result == 0) {
            globalPermissionDao.insertGlobalPermission(request);
        }
    }

    /**
     * {@inheritDoc}
     *
     * 用户风控配置参数为商户级风控配置和用户级风控配置的并集，当存在用户级配置参数则优先使用用户级参数，否则使用商户级配置参数;
     * 当两则都不存在时，则返回不受任何限制的空配置
     */
    @Override
    public PermissionDTO loadUserPermission(Long mchId, Long accountId) {
        // 创建不受任何限制的空配置
        AtomicInteger permission = new AtomicInteger(Permission.ALL_PERMISSION);
        DepositPermission deposit = new DepositPermission();
        WithdrawPermission withdraw = new WithdrawPermission();
        TradePermission trade = new TradePermission();

        // 加载商户级风控配置
        Optional<GlobalPermission> globalOpt = globalPermissionDao.findByMchId(mchId);
        globalOpt.ifPresent(global -> {
            permission.set(global.getPermission());
            deposit.override(JsonUtils.fromJsonString(global.getDeposit(), DepositPermission.class));
            withdraw.override(JsonUtils.fromJsonString(global.getWithdraw(), WithdrawPermission.class));
            trade.override(JsonUtils.fromJsonString(global.getTrade(), TradePermission.class));
        });
        // 加载用户级风控配置，并覆盖商户级风控配置
        Optional<UserPermission> userOpt = userPermissionDao.findByAccountId(accountId);
        userOpt.ifPresent(user -> {
            permission.set(user.getPermission());
            deposit.override(JsonUtils.fromJsonString(user.getDeposit(), DepositPermission.class));
            withdraw.override(JsonUtils.fromJsonString(user.getWithdraw(), WithdrawPermission.class));
            trade.override(JsonUtils.fromJsonString(user.getTrade(), TradePermission.class));
        });

        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPermission(permission.get());
        permissionDTO.setDeposit(deposit);
        permissionDTO.setWithdraw(withdraw);
        permissionDTO.setTrade(trade);
        return permissionDTO;
    }

    /**
     * {@inheritDoc}
     *
     * 如存在，则更新配置，否则新增配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPermission(Long accountId, PermissionDTO permission) {
        LocalDateTime now = LocalDateTime.now();
        // 默认空配置
        int bitmap = Permission.NO_PERMISSION;
        DepositPermission deposit = new DepositPermission();
        WithdrawPermission withdraw = new WithdrawPermission();
        TradePermission trade = new TradePermission();

        // 利用传入的配置参数进行参数覆盖
        if (permission.getPermission() != null) {
            bitmap = permission.getPermission();
        }
        deposit.override(permission.getDeposit());
        withdraw.override(permission.getWithdraw());
        trade.override(permission.getTrade());

        UserPermission request = UserPermission.builder().accountId(accountId).permission(bitmap)
            .deposit(JsonUtils.toJsonString(deposit)).withdraw(JsonUtils.toJsonString(withdraw))
            .trade(JsonUtils.toJsonString(trade)).createdTime(now).modifiedTime(now).build();
        int result = userPermissionDao.updateUserPermission(request);
        if (result == 0) {
            userPermissionDao.insertUserPermission(request);
        }
    }
}
