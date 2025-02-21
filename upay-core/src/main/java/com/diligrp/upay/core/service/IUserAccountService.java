package com.diligrp.upay.core.service;

import com.diligrp.upay.core.domain.RegisterAccount;
import com.diligrp.upay.core.model.UserAccount;

import java.util.List;

/**
 * 用户账户服务接口
 */
public interface IUserAccountService {

    /**
     * 创建资金账号
     */
    long createUserAccount(Long mchId, RegisterAccount account);

    /**
     * 冻结资金账号
     */
    void freezeUserAccount(Long accountId);

    /**
     * 解冻资金账号
     */
    void unfreezeUserAccount(Long accountId);

    /**
     * 注销资金账号
     */
    void unregisterUserAccount(Long mchId, Long accountId);

    /**
     * 查询用户账户
     */
    UserAccount findUserAccountById(Long accountId);

    /**
     * 查询指定商户下的用户账户
     */
    UserAccount findUserAccountById(Long mchId, Long accountId);
}
