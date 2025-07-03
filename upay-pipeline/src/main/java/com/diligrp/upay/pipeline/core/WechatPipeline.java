package com.diligrp.upay.pipeline.core;

import com.diligrp.upay.core.exception.PaymentServiceException;
import com.diligrp.upay.pipeline.client.WechatHttpClient;
import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.WechatCertificate;
import com.diligrp.upay.pipeline.domain.wechat.WechatConfig;
import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.pipeline.type.ChannelType;
import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.pipeline.util.WechatSignatureUtils;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.security.RsaCipher;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.shared.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * 微信支付通道抽象模型
 */
public abstract class WechatPipeline extends PaymentPipeline<WechatPipeline.WechatParams> {

    private static final Logger LOG = LoggerFactory.getLogger(WechatPipeline.class);

    // 微信支付通道配置
    protected WechatConfig wechatConfig;

    public WechatPipeline(long mchId, long pipelineId, String name, String uri, String params) throws Exception {
        super(mchId, pipelineId, name, uri, params);

        AssertUtils.notEmpty(params().notifyBaseUri, String.format("微信支付缺少参数配置: notifyBaseUri"));
    }

    /**
     * 微信支付通道配置
     *
     * @param mchId - 微信商户号
     * @param appId - 微信小程序ID
     * @param appSecret - 小程序密钥
     * @param serialNo - 商户公钥序列号
     * @param privateKeyStr - BASE64编码的商户私钥
     * @param wechatSerialNo - 微信平台公钥序列号
     * @param wechatPublicKey - BASE64编码的微信平台公钥
     * @param apiV3KeyStr - BASE64编码的apiV3Key
     */
    public void configure(String mchId, String appId, String appSecret, String serialNo, String privateKeyStr,
                          String wechatSerialNo, String wechatPublicKey, String apiV3KeyStr) {
        checkParam("微信商户号", mchId);
        checkParam("微信小程序ID", appId);
        checkParam("小程序密钥", appSecret);
        checkParam("商户公钥序列号", serialNo);
        checkParam("商户私钥", privateKeyStr);
        checkParam("微信平台公钥序列号", wechatSerialNo);
        checkParam("微信平台公钥", wechatPublicKey);
        checkParam("微信平台apiV3Key", apiV3KeyStr);

        PrivateKey privateKey;
        try {
            privateKey = RsaCipher.getPrivateKey(privateKeyStr);
        } catch (Exception e) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "商户私钥配置错误");
        }

        byte[] keyBytes = apiV3KeyStr.getBytes(StandardCharsets.UTF_8); // 需32位
        if (keyBytes.length != WechatConstants.KEY_LENGTH_BYTE) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "微信支付平台apiV3Key配置错误");
        }
        SecretKeySpec apiV3Key = new SecretKeySpec(keyBytes, WechatConstants.AES_ALGORITHM);

        wechatConfig = new WechatConfig(mchId, appId, appSecret, serialNo, privateKey, apiV3Key);
        try {
            wechatConfig.putCertificate(WechatCertificate.of(wechatSerialNo, RsaCipher.getPublicKey(wechatPublicKey)));
        } catch (Exception ex) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "微信支付平台公钥配置错误");
        }
    }

    public NativePrepayResponse sendNativePrepayRequest(WechatPrepayRequest request) {
        try {
            String notifyUri = String.format("%s%s?%s=%s", params().notifyBaseUri, WechatConstants.PAYMENT_NOTIFY_URI,
                WechatConstants.PARAM_PIPELINE, pipelineId());
            String qrCode = getClient().sendNativePrepayRequest(request, notifyUri);
            return NativePrepayResponse.of(request.getPaymentId(), qrCode);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Send wechat native prepay request exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信预支付失败");
        }
    }

    public JsApiPrepayResponse sendJsApiPrepayRequest(WechatPrepayRequest request) {
        try {
            String notifyUri = String.format("%s%s?%s=%s", params().notifyBaseUri, WechatConstants.PAYMENT_NOTIFY_URI,
                WechatConstants.PARAM_PIPELINE, pipelineId());
            String prepayId = getClient().sendJsApiPrepayRequest(request, notifyUri);

            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = RandomUtils.randomString(32);
            String packet = "prepay_id=" + prepayId;
            String appId = getClient().getWechatConfig().getAppId();
            String message = String.format("%s\n%s\n%s\n%s\n", appId, timeStamp, nonceStr, packet);
            String paySign = WechatSignatureUtils.signature(message, getClient().getWechatConfig().getPrivateKey());
            String signType = WechatConstants.RSA_ALGORITHM;
            return JsApiPrepayResponse.of(request.getPaymentId(), prepayId, timeStamp, nonceStr, signType, paySign);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Send wechat jsApi prepay request exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信预支付失败");
        }
    }

    public WechatPaymentResponse queryPrepayResponse(WechatPrepayQuery request) {
        try {
            return getClient().queryPrepayResponse(request);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Query wechat prepay order request exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "查询微信支付状态失败");
        }
    }

    public void closePrepayOrder(WechatPrepayClose request) {
        try {
            getClient().closePrepayOrder(request);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Close wechat prepay order exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "关闭微信预支付订单失败");
        }
    }

    public WechatRefundResponse sendRefundRequest(WechatRefundRequest request) {
        try {
            String notifyUri = String.format("%s%s?%s=%s", params().notifyBaseUri, WechatConstants.REFUND_NOTIFY_URI,
                WechatConstants.PARAM_PIPELINE, pipelineId());
            return getClient().sendRefundRequest(request, notifyUri);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Send wechat refund request exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信退款失败");
        }
    }

    public WechatRefundResponse queryRefundResponse(WechatRefundQuery request) {
        try {
            return getClient().queryRefundOrder(request);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("Query wechat refund order exception", ex);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "查询微信退款失败");
        }
    }

    public abstract WechatHttpClient getClient();

    @Override
    public ChannelType supportedChannel() {
        return ChannelType.WXPAY;
    }

    @Override
    public Class paramClass() {
        return WechatParams.class;
    }

    public static class WechatParams extends PipelineParams {
        // 微信支付通知base地址，如: https://gateway.diligrp.com/pay-service
        private String notifyBaseUri;

        public WechatParams(String params) {
            super(params);
        }

        public String getNotifyBaseUri() {
            return notifyBaseUri;
        }

        public void setNotifyBaseUri(String notifyBaseUri) {
            this.notifyBaseUri = notifyBaseUri;
        }
    }
}
