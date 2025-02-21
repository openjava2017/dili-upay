package com.diligrp.upay.core.domain;

/**
 * 商户接入许可
 */
public class MerchantPermit {
    // 商户ID
    private Long mchId;
    // 商户名称
    private String name;
    // 父商户ID
    private Long parentId;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;

    public Long getMchId() {
        return mchId;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getProfitAccount() {
        return profitAccount;
    }

    public Long getVouchAccount() {
        return vouchAccount;
    }

    public Long getPledgeAccount() {
        return pledgeAccount;
    }

    public Long parentMchId() {
        return getParentId() == 0 ? getMchId() : getParentId();
    }

    public static MerchantPermit of(Long mchId, String name, Long parentId, Long profitAccount,
                                    Long vouchAccount, Long pledgeAccount) {
        MerchantPermit permit = new MerchantPermit();
        permit.mchId = mchId;
        permit.name = name;
        permit.parentId = parentId;
        permit.profitAccount = profitAccount;
        permit.vouchAccount = vouchAccount;
        permit.pledgeAccount = pledgeAccount;
        return permit;
    }
}
