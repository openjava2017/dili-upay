package com.diligrp.upay.trade.domain;

import com.diligrp.upay.shared.domain.PageQuery;

import java.time.LocalDateTime;

/**
 * 冻结订单查询
 */
public class FrozenOrderQuery extends PageQuery {
    // 页号
    private Integer pageNo = 1;
    // 每页记录数
    private Integer pageSize = 20;
    // 资金账号
    private Long accountId;
    // 冻结类型-系统冻结 交易冻结
    private Integer type;
    // 冻结状态
    private Integer state;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
