package com.diligrp.upay.core.dao;


import com.diligrp.upay.core.model.FundAccount;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 账户资金数据访问层
 */
@Repository("fundAccountDao")
public interface IFundAccountDao extends MybatisMapperSupport {

    void insertFundAccount(FundAccount fund);

    Optional<FundAccount> findByAccountId(Long accountId);

    Optional<FundAccount> lockByAccountId(Long accountId);

    int updateByAccountId(FundAccount fundAccount);

    int compareAndSetVersion(FundAccount fundAccount);

    Optional<FundAccount> sumCustomerFund(@Param("mchId") Long mchId, @Param("customerId") Long customerId);

    List<FundAccount> listFundAccounts(@Param("mchId") Long mchId, @Param("customerId") Long customerId);
}
