package com.diligrp.upay.sentinel.dao;

import com.diligrp.upay.sentinel.model.GlobalPermission;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 全局权限数据访问层
 */
@Repository("globalPermissionDao")
public interface IGlobalPermissionDao extends MybatisMapperSupport {

    /**
     * 查找全局风控配置
     */
    Optional<GlobalPermission> findByMchId(Long mchId);

    /**
     * 设置全局风控配置
     */
    void insertGlobalPermission(GlobalPermission permission);

    /**
     * 更新全局风控配置
     */
    int updateGlobalPermission(GlobalPermission permission);
}