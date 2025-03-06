package com.diligrp.upay.pipeline.impl;

import com.diligrp.upay.pipeline.client.WechatHttpClient;
import com.diligrp.upay.pipeline.client.WechatPartnerHttpClient;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.shared.util.AssertUtils;

/**
 * 微信支付通道抽象模型-服务商模式
 */
public class WechatPartnerPipeline extends WechatPipeline {

    // 服务商模式下的微信客户端
    private WechatPartnerHttpClient client;

    public WechatPartnerPipeline(long mchId, long pipelineId, String name, String uri, String params) throws Exception {
        super(mchId, pipelineId, name, uri, params);
    }

    public void configure(String mchId, String appId, String appSecret, String serialNo, String privateKeyStr,
                          String publicKeyNo, String publicKeyStr, String apiV3KeyStr) {
        super.configure(mchId, appId, appSecret, serialNo, privateKeyStr, publicKeyNo, publicKeyStr, apiV3KeyStr);
        client = new WechatPartnerHttpClient(uri(), wechatConfig);
    }

    @Override
    public WechatHttpClient getClient() {
        AssertUtils.notNull(client, "微信支付通道配置错误");
        return client;
    }
}
