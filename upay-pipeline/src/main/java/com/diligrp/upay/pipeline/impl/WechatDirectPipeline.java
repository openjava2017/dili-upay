package com.diligrp.upay.pipeline.impl;

import com.diligrp.upay.pipeline.client.WechatDirectHttpClient;
import com.diligrp.upay.pipeline.client.WechatHttpClient;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.shared.util.AssertUtils;

/**
 * 微信支付通道抽象模型-直联模式
 */
public class WechatDirectPipeline extends WechatPipeline {

    // 直联模式下的微信客户端
    private WechatDirectHttpClient client;

    public WechatDirectPipeline(long mchId, long pipelineId, String name, String uri, String params) throws Exception {
        super(mchId, pipelineId, name, uri, params);
    }

    public void configure(String mchId, String appId, String appSecret, String serialNo, String privateKeyStr,
                          String wechatSerialNo, String wechatPublicKey, String apiV3KeyStr) {
        super.configure(mchId, appId, appSecret, serialNo, privateKeyStr, wechatSerialNo, wechatPublicKey, apiV3KeyStr);
        client = new WechatDirectHttpClient(uri(), wechatConfig);
    }

    @Override
    public WechatHttpClient getClient() {
        AssertUtils.notNull(client, "微信支付通道配置错误");
        return client;
    }
}
