package com.diligrp.upay.pipeline.domain;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.Message;

import java.util.List;

/**
 * 支持统计的分页查询结果数据模型
 */
public class SumPageMessage<T> extends Message<List<T>> {
    // 总记录数
    private long total;
    // 总收入
    private long income;
    // 总支出
    private long output;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getOutput() {
        return output;
    }

    public void setOutput(long output) {
        this.output = output;
    }

    public static <T> SumPageMessage<T> success(long total, List<T> data, long income, long output) {
        SumPageMessage<T> result = new SumPageMessage<>();
        result.setCode(CODE_SUCCESS);
        result.setTotal(total);
        result.setData(data);
        result.setIncome(income);
        result.setOutput(output);
        result.setMessage(MSG_SUCCESS);
        return result;
    }

    public static SumPageMessage<?> failure(String message) {
        SumPageMessage<?> result = new SumPageMessage<>();
        result.setCode(ErrorCode.SYSTEM_UNKNOWN_ERROR);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    public static SumPageMessage<?> failure(int code, String message) {
        SumPageMessage<?> result = new SumPageMessage<>();
        result.setCode(code);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }
}
