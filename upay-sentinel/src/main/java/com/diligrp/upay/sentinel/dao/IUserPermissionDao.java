package com.diligrp.upay.sentinel.dao;

import com.diligrp.upay.sentinel.model.UserPermission;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户权限数据访问层
 */
@Repository("userPermissionDao")
public interface IUserPermissionDao extends MybatisMapperSupport {
    /**
     * 查找账户风控配置
     */
    Optional<UserPermission> findByAccountId(Long accountId);

    /**
     * 设置账户风控配置
     */
    void insertUserPermission(UserPermission permission);

    /**
     * 更新账户风控配置
     */
    int updateUserPermission(UserPermission permission);
}