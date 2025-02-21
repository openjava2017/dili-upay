package com.diligrp.upay.sentinel.service;

import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.sentinel.domain.PasswordDTO;

/**
 * 密码管理及验证服务
 */
public interface IUserPasswordService {
    /**
     * 修改账户密码
     * 密码错误次数不能超过当日最大限制，否则将锁定账户
     */
    void changeUserPassword(UserAccount account, PasswordDTO request, int maxPwdErrors);

    /**
     * 验证账户密码
     * 密码错误次数不能超过当日最大限制，否则将锁定账户
     */
    void checkUserPassword(UserAccount account, String password, int maxPwdErrors);

    /**
     * 重置账户密码：锁定的账号重置密码后状态变为正常
     */
    void resetUserPassword(UserAccount account, String password);
}
