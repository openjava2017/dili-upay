package com.diligrp.upay.trade.dao;

import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import com.diligrp.upay.trade.model.UserProtocol;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 免密支付协议数据访问层
 */
@Repository("userProtocolDao")
public interface IUserProtocolDao extends MybatisMapperSupport {
    void insertUserProtocol(UserProtocol protocol);

    Optional<UserProtocol> findByAccountId(@Param("accountId") Long accountId, @Param("type") Integer type);

    Optional<UserProtocol> findByProtocolId(Long protocolId);

    int compareAndSetState(UserProtocol protocol);
}
