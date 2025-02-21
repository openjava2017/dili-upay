package com.diligrp.upay.core.dao;

import com.diligrp.upay.core.model.Application;
import com.diligrp.upay.core.model.Merchant;
import com.diligrp.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("merchantDao")
public interface IMerchantDao extends MybatisMapperSupport {
    void insertMerchant(Merchant merchant);

    Optional<Merchant> findByMchId(Long mchId);

    int updateMerchant(Merchant merchant);

    void insertApplication(Application application);

    Optional<Application> findByAppId(Long mchId);
}
