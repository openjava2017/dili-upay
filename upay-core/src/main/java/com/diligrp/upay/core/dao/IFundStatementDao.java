package com.diligrp.upay.core.dao;

import com.diligrp.upay.core.model.FundStatement;
import com.diligrp.upay.core.util.DataPartition;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金收支明细数据访问层
 */
@Repository("fundStatementDao")
public interface IFundStatementDao extends MybatisMapperSupport {
    void insertFundStatements(@Param("strategy") DataPartition strategy, @Param("statements") List<FundStatement> statements);

    void insertVouchStatements(@Param("strategy") DataPartition strategy, @Param("statements") List<FundStatement> statements);

}