package com.diligrp.upay.pipeline.service.impl;

import com.diligrp.upay.core.domain.FundTransaction;
import com.diligrp.upay.core.domain.TransactionStatus;
import com.diligrp.upay.core.service.IFundStreamEngine;
import com.diligrp.upay.pipeline.service.IAccountPipelineService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service("accountPipelineService")
public class AccountPipelineServiceImpl implements IAccountPipelineService {

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Override
    public TransactionStatus submit(Supplier<FundTransaction> supplier) {
        FundTransaction transaction = supplier.get();
        return fundStreamEngine.submit(transaction);
    }

    @Override
    public TransactionStatus submitExclusively(Supplier<FundTransaction> supplier) {
        FundTransaction transaction = supplier.get();
        return fundStreamEngine.submitExclusively(transaction);
    }
}
