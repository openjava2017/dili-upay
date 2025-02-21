package com.diligrp.upay.boot.component;

import com.diligrp.upay.boot.domain.AccountId;
import com.diligrp.upay.boot.domain.MerchantId;
import com.diligrp.upay.boot.domain.Option;
import com.diligrp.upay.boot.domain.RiskControl;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.core.service.IUserAccountService;
import com.diligrp.upay.sentinel.domain.*;
import com.diligrp.upay.sentinel.service.IRiskControlService;
import com.diligrp.upay.sentinel.service.IUserPasswordService;
import com.diligrp.upay.shared.domain.ServiceRequest;
import com.diligrp.upay.shared.sapi.CallableComponent;
import com.diligrp.upay.shared.util.AssertUtils;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 风控管理模块
 */
@CallableComponent(id = "payment.sentinel.service")
public class RiskControlComponent {

    @Resource
    private IRiskControlService riskControlService;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IUserPasswordService userPasswordService;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 获取商户级权限及风控配置
     */
    public RiskControl loadGlobal(ServiceRequest<MerchantId> request) {
        MerchantId merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");

        accessPermitService.loadMerchantPermit(merchant.getMchId());
        PermissionDTO permission = riskControlService.loadGlobalPermission(merchant.getMchId());
        RiskControl rc = fromPermissionDTO(permission);
        rc.setMchId(merchant.getMchId());
        return rc;
    }

    /**
     * 设置商户级权限及风控配置
     */
    public void setGlobal(ServiceRequest<RiskControl> request) {
        RiskControl rc = request.getData();
        AssertUtils.notNull(rc.getMchId(), "mchId missed");
        AssertUtils.notNull(rc.getPermissions(), "permissions missed");
        accessPermitService.loadMerchantPermit(rc.getMchId());

        checkDepositPermission(rc.getDeposit());
        checkWithdrawPermission(rc.getWithdraw());
        checkTradePermission(rc.getTrade());

        PermissionDTO permissionDTO = fromRiskControl(rc);
        riskControlService.saveGlobalPermission(rc.getMchId(), permissionDTO);
    }

    /**
     * 获取用户权限及风控配置
     */
    public RiskControl loadUser(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        // 获取账户已有权限
        UserAccount account = userAccountService.findUserAccountById(accountId.getAccountId());
        PermissionDTO permissionDTO = riskControlService.loadUserPermission(account.getMchId(), account.getAccountId());
        RiskControl rc = fromPermissionDTO(permissionDTO);
        rc.setAccountId(accountId.getAccountId());
        return rc;
    }

    /**
     * 设置用户权限及风控配置
     */
    public void setUser(ServiceRequest<RiskControl> request) {
        RiskControl rc = request.getData();
        AssertUtils.notNull(rc.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(rc.getPassword(), "password missed");
        AssertUtils.notNull(rc.getPermissions(), "permissions missed");

        checkDepositPermission(rc.getDeposit());
        checkWithdrawPermission(rc.getWithdraw());
        checkTradePermission(rc.getTrade());

        UserAccount account = userAccountService.findUserAccountById(rc.getAccountId());
        userPasswordService.checkUserPassword(account, rc.getPassword(), -1);

        PermissionDTO permissionDTO = fromRiskControl(rc);
        riskControlService.saveUserPermission(account.getAccountId(), permissionDTO);
    }

    private void checkDepositPermission(DepositPermission deposit) {
        if (deposit != null) {
            checkNumber(deposit.getMaxAmount(), "单笔充值限额设置非法");
        }
    }

    private void checkWithdrawPermission(WithdrawPermission withdraw) {
        if (withdraw != null) {
            checkNumber(withdraw.getMaxAmount(), "单笔提现限额设置非法");
            checkNumber(withdraw.getDailyAmount(), "日提现限额设置非法");
            checkNumber(withdraw.getDailyTimes(), "日提现次数设置非法");
            checkNumber(withdraw.getMonthlyAmount(), "月提现限额设置非法");
        }
    }

    private void checkTradePermission(TradePermission trade) {
        if (trade != null) {
            checkNumber(trade.getMaxAmount(), "单笔交易限额设置非法");
            checkNumber(trade.getDailyAmount(), "日交易限额设置非法");
            checkNumber(trade.getDailyTimes(), "日交易次数设置非法");
            checkNumber(trade.getMonthlyAmount(), "月交易限额设置非法");
        }
    }

    private RiskControl fromPermissionDTO(PermissionDTO permission) {
        List<Integer> permissions = Permission.permissions(permission.getPermission()).stream().map(Permission::getCode)
            .collect(Collectors.toList());
        List<Option> allPermission = Permission.getPermissions().stream().map(p -> Option.of(p.getCode(), p.getName()))
            .collect(Collectors.toList());

        RiskControl response = new RiskControl();
        response.setPermissions(permissions);
        response.setAllPermission(allPermission);
        response.setDeposit(permission.getDeposit());
        response.setWithdraw(permission.getWithdraw());
        response.setTrade(permission.getTrade());
        return response;
    }

    private PermissionDTO fromRiskControl(RiskControl rc) {
        Permission[] permissions = rc.getPermissions().stream().map(Permission::getPermission)
            .map(p -> p.orElseThrow(() -> new IllegalArgumentException("Invalid permission code"))).toArray(Permission[]::new);
        int permission = Permission.permissionMask(permissions);

        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setPermission(permission);
        permissionDTO.setDeposit(rc.getDeposit());
        permissionDTO.setWithdraw(rc.getWithdraw());
        permissionDTO.setTrade(rc.getTrade());
        return  permissionDTO;
    }

    private void checkNumber(Number number, String message) {
        AssertUtils.isTrue(number == null || number.longValue() > 0, message);
    }
}
