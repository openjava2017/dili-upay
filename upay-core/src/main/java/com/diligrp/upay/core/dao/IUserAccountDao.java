package com.diligrp.upay.core.dao;

import com.diligrp.upay.core.domain.AccountStateDto;
import com.diligrp.upay.core.model.UserAccount;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 资金账户数据访问层
 */
@Repository("userAccountDao")
public interface IUserAccountDao extends MybatisMapperSupport {

    void insertUserAccount(UserAccount account);

    /**
     * 根据账号ID（非主键）查询资金账号
     */
    Optional<UserAccount> findByAccountId(Long accountId);

    /**
     * 根据主账户ID查询子账户列表
     */
    List<UserAccount> listByParentId(Long parentId);

    /**
     * 修改资金账号状态，根据数据版本（乐观锁）判断记录是否被修改
     */
    Integer compareAndSetState(AccountStateDto accountState);

    /**
     * 修改资金账号信息
     */
    Integer updateUserAccount(UserAccount account);
}
