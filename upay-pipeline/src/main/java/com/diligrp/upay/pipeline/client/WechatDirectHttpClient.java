package com.diligrp.upay.pipeline.client;

import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.ErrorMessage;
import com.diligrp.upay.pipeline.domain.wechat.WechatConfig;
import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.pipeline.util.WechatSignatureUtils;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.util.DateUtils;
import com.diligrp.upay.shared.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付HTTP客户端-直联模式
 */
public class WechatDirectHttpClient extends WechatHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(WechatDirectHttpClient.class);

    private static final String NATIVE_PREPAY = "/v3/pay/transactions/native";

    private static final String JSAPI_PREPAY = "/v3/pay/transactions/jsapi";
    // 查询订单状态
    private static final String TRANSACTION_QUERY = "/v3/pay/transactions/out-trade-no/%s?mchid=%s";
    // 关闭订单
    private static final String TRANSACTION_CLOSE = "/v3/pay/transactions/out-trade-no/%s/close";
    // 退款
    private static final String TRANSACTION_REFUND = "/v3/refund/domestic/refunds";
    // 退款查询
    private static final String REFUND_QUERY = "/v3/refund/domestic/refunds/%s";

    // 退款
    public WechatDirectHttpClient(String wechatBaseUri, WechatConfig wechatConfig) {
        super(wechatBaseUri, wechatConfig);
    }

    /**
     * Native支付下单, 返回二维码链接
     */
    @Override
    public String sendNativePrepayRequest(WechatPrepayRequest request, String notifyUri) throws Exception {
        String payload = nativePrepayRequest(request, notifyUri);
        // 获取认证信息和签名信息
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_POST,
            NATIVE_PREPAY, payload, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());
        HttpHeader[] headers = new HttpHeader[3];
        headers[0] = HttpHeader.create(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        headers[1] = HttpHeader.create(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON);
        headers[2] = HttpHeader.create(WechatConstants.HEADER_AUTHORIZATION, authorization);

        LOG.info("Sending wechat native prepay request...");
        LOG.debug("Authorization: {}\n{}", authorization, payload);
        HttpResult result = send(wechatBaseUri + NATIVE_PREPAY, headers, payload);
        verifyHttpResult(result);
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回
            Map<String, Object> response = JsonUtils.fromJsonString(result.responseText, Map.class);
            return (String) response.get("code_url");
        } else {
            LOG.error("Wechat native prepay failed: {}\n{}", result.statusCode, result.responseText);
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信预支付失败: " + message.getMessage());
        }
    }

    /**
     * 查询微信支付订单状态
     */
    @Override
    public String sendJsApiPrepayRequest(WechatPrepayRequest request, String notifyUri) throws Exception {
        String payload = jsApiPrepayRequest(request, notifyUri);
        // 获取认证信息和签名信息
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_POST, JSAPI_PREPAY,
            payload, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());
        HttpHeader[] headers = new HttpHeader[3];
        headers[0] = HttpHeader.create(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        headers[1] = HttpHeader.create(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON);
        headers[2] = HttpHeader.create(WechatConstants.HEADER_AUTHORIZATION, authorization);

        LOG.info("Sending wechat jsapi prepay request...");
        LOG.debug("Authorization: {}\n{}", authorization, payload);
        HttpResult result = send(wechatBaseUri + JSAPI_PREPAY, headers, payload);
        verifyHttpResult(result);
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回
            Map<String, Object> response = JsonUtils.fromJsonString(result.responseText, Map.class);
            return (String) response.get("prepay_id");
        } else {
            LOG.error("Wechat jsapi prepay failed: {}\n{}", result.statusCode, result.responseText);
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信预支付失败: " + message.getMessage());
        }
    }

    @Override
    public WechatPaymentResponse queryPrepayResponse(WechatPrepayQuery request) throws Exception {
        // 获取认证信息和签名信息
        String uri = String.format(TRANSACTION_QUERY, request.getPaymentId(), wechatConfig.getMchId());
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_GET, uri,
            wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());

        HttpRequest.Builder httpRequest = HttpRequest.newBuilder().uri(URI.create(wechatBaseUri + uri))
            .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(MAX_REQUEST_TIMEOUT_TIME))
            .header(CONTENT_TYPE, CONTENT_TYPE_JSON).header(WechatConstants.HEADER_AUTHORIZATION, authorization)
            .header(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON)
            .header(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        LOG.info("Sending wechat query merchant order request...");
        LOG.debug("Authorization: {}\n", authorization);
        HttpResult result = execute(httpRequest.GET().build());
        verifyHttpResult(result);
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回(关闭的订单trade_state = CLOSED)
            Map<String, Object> response = JsonUtils.fromJsonString(result.responseText, Map.class);
            LocalDateTime when = DateUtils.parseDateTime((String)response.get("success_time"), WechatConstants.RFC3339_FORMAT);
            Map<String, Object> payer = (Map<String, Object>) response.get("payer");
            String openId = payer == null ? null : (String) payer.get("openid");
            return WechatPaymentResponse.of((String) response.get("out_trade_no"), (String) response.get("transaction_id"),
                openId, when, (String) response.get("trade_state"), (String) response.get("trade_state_desc"));
        } else {
            LOG.info("Wechat query transaction status failed: {}", result.statusCode);
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信订单查询失败: " + message.getMessage());
        }
    }

    /**
     * 关闭微信订单 - 关闭订单不会回调通知
     * 返回码204标识关闭成功, 其他错误码表明关闭异常失败;
     * 已支付完成的订单调用关闭返回400, 订单不存在返回404
     */
    @Override
    public void closePrepayOrder(WechatPrepayClose request) throws Exception {
        // 获取认证信息和签名信息
        String uri = String.format(TRANSACTION_CLOSE, request.getPaymentId());
        String payload = JsonUtils.toJsonString(Merchant.of(wechatConfig.getMchId()));
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_POST, uri,
            payload, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());

        HttpHeader[] headers = new HttpHeader[3];
        headers[0] = HttpHeader.create(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        headers[1] = HttpHeader.create(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON);
        headers[2] = HttpHeader.create(WechatConstants.HEADER_AUTHORIZATION, authorization);
        LOG.info("Sending close wechat prepay order request...");
        LOG.debug("Authorization: {}\n{}", authorization, payload);
        HttpResult result = send(wechatBaseUri + uri, headers, payload);
        verifyHttpResult(result);
        LOG.info("Close wechat prepay order statusCode: {}", result.statusCode);
        if (result.statusCode != 200 && result.statusCode != 204) {
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.INVALID_OBJECT_STATE, "关闭微信订单失败: " + message.getMessage());
        }
    }

    /**
     * 申请微信支付退款
     */
    @Override
    public WechatRefundResponse sendRefundRequest(WechatRefundRequest request, String notifyUri) throws Exception {
        String payload = refundRequest(request, notifyUri);
        // 获取认证信息和签名信息
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_POST,
            TRANSACTION_REFUND, payload, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());
        HttpHeader[] headers = new HttpHeader[3];
        headers[0] = HttpHeader.create(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        headers[1] = HttpHeader.create(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON);
        headers[2] = HttpHeader.create(WechatConstants.HEADER_AUTHORIZATION, authorization);

        LOG.info("Sending wechat payment refund request...");
        LOG.debug("Authorization: {}\n{}", authorization, payload);
        HttpResult result = send(wechatBaseUri + TRANSACTION_REFUND, headers, payload);
        verifyHttpResult(result);
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回
            Map<String, Object> response = JsonUtils.fromJsonString(result.responseText, Map.class);
            LocalDateTime when = DateUtils.parseDateTime((String) response.get("success_time"), WechatConstants.RFC3339_FORMAT);
            return WechatRefundResponse.of((String) response.get("out_refund_no"), (String) response.get("refund_id"),
                when, (String) response.get("status"), (String) response.get("status"));
        } else {
            LOG.info("send wechat refund failed: {}", result.statusCode);
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "发起微信退款失败: " + message.getMessage());
        }
    }

    /**
     * 微信支付退款查询
     */
    @Override
    public WechatRefundResponse queryRefundOrder(WechatRefundQuery request) throws Exception {
        String uri = String.format(REFUND_QUERY, request.getRefundId());
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_GET,
            uri, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());

        HttpRequest.Builder httpRequest = HttpRequest.newBuilder().uri(URI.create(wechatBaseUri + uri))
            .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(MAX_REQUEST_TIMEOUT_TIME))
            .header(CONTENT_TYPE, CONTENT_TYPE_JSON).header(WechatConstants.HEADER_AUTHORIZATION, authorization)
            .header(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON)
            .header(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        LOG.info("Sending wechat refund query request...");
        LOG.debug("Authorization: {}\n", authorization);
        HttpResult result = execute(httpRequest.GET().build());
        verifyHttpResult(result);
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回
            Map<String, Object> response = JsonUtils.fromJsonString(result.responseText, Map.class);
            LocalDateTime when = DateUtils.parseDateTime((String) response.get("success_time"), WechatConstants.RFC3339_FORMAT);
            return WechatRefundResponse.of((String) response.get("out_refund_no"), (String) response.get("refund_id"),
                when, (String) response.get("status"), (String) response.get("user_received_account"));
        } else if (result.statusCode == 404) {
            throw new PaymentPipelineException(ErrorCode.OBJECT_NOT_FOUND, "发起微信退款查询失败: 退款单不存在");
        } else {
            LOG.info("Wechat refund query failed: {}", result.statusCode);
            ErrorMessage message = JsonUtils.fromJsonString(result.responseText, ErrorMessage.class);
            throw new PaymentPipelineException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "微信退款查询失败: " + message.getMessage());
        }
    }

    private String nativePrepayRequest(WechatPrepayRequest request, String notifyUri) {
        Map<String, Object> params = new HashMap<>();
        params.put("mchid", wechatConfig.getMchId());
        params.put("appid", wechatConfig.getAppId());
        params.put("description", request.getGoods());
        params.put("out_trade_no", request.getPaymentId());
        params.put("notify_url", notifyUri);
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", request.getAmount());
        amount.put("currency", "CNY");
        params.put("amount", amount);

        return JsonUtils.toJsonString(params);
    }

    private String jsApiPrepayRequest(WechatPrepayRequest request, String notifyUri) {
        Map<String, Object> params = new HashMap<>();
        params.put("mchid", wechatConfig.getMchId());
        params.put("appid", wechatConfig.getAppId());
        params.put("description", request.getGoods());
        params.put("out_trade_no", request.getPaymentId());
        params.put("notify_url", notifyUri);
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", request.getAmount());
        amount.put("currency", "CNY");
        params.put("amount", amount);
        Map<String, Object> payer = new HashMap<>();
        payer.put("openid", request.getOpenId());
        params.put("payer", payer);
        return JsonUtils.toJsonString(params);
    }

    private String refundRequest(WechatRefundRequest request, String notifyUri) {
        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", request.getPaymentId());
        params.put("out_refund_no", request.getRefundId());
        params.put("reason", request.getDescription());
        params.put("notify_url", notifyUri);
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", request.getMaxAmount());
        amount.put("refund", request.getAmount());
        amount.put("currency", "CNY");
        params.put("amount", amount);
        return JsonUtils.toJsonString(params);
    }

    private static class Merchant {
        // 商户号
        private String mchid;

        public static Merchant of(String mchId) {
            Merchant merchant = new Merchant();
            merchant.setMchid(mchId);
            return merchant;
        }

        public String getMchid() {
            return mchid;
        }

        public void setMchid(String mchid) {
            this.mchid = mchid;
        }
    }
}
