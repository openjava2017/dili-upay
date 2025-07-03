package com.diligrp.upay.pipeline.domain.wechat;

import com.diligrp.upay.shared.domain.PageQuery;

import java.time.LocalDateTime;

/**
 * 客户通道支付流水查询申请
 */
public class WechatStatementQuery extends PageQuery {
    // 页号
    private Integer pageNo = 1;
    // 每页记录数
    private Integer pageSize = 20;
    // 商户ID
    private Long mchId;
    // 交易类型
    private Integer type;
    // 客户ID
    private Long customerId;
    // 交易订单号
    private String paymentId;
    // 业务单号
    private String outTradeNo;
    // 通道编码
    private Long pipelineId;
    // 交易状态
    private Integer state;
    // 开始日期
    private LocalDateTime startDate;
    // 结束日期
    private LocalDateTime endDate;

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

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
