package com.diligrp.upay.pipeline.model;

import com.diligrp.upay.shared.model.BaseDO;

public class PaymentPipeline extends BaseDO {
    // 商户ID
    private Long mchId;
    // 通道ID
    private Long pipelineId;
    // 支付渠道
    private Integer channelId;
    // 通道类型
    private Integer type;
    // 通道名称
    private String name;
    // 通道uri
    private String uri;
    // 通道参数
    private String param;
    // 通道状态
    private Integer state;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
