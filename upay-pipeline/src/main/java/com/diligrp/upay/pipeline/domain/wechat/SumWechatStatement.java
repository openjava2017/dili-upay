package com.diligrp.upay.pipeline.domain.wechat;

public class SumWechatStatement {
    // 总记录数
    private Long total;
    // 总收入
    private Long income;
    // 总支出
    private Long output;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getIncome() {
        return income;
    }

    public void setIncome(Long income) {
        this.income = income;
    }

    public Long getOutput() {
        return output;
    }

    public void setOutput(Long output) {
        this.output = output;
    }
}
