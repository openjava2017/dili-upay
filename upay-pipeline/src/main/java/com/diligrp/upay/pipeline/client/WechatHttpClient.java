package com.diligrp.upay.pipeline.client;

import com.diligrp.upay.pipeline.domain.*;
import com.diligrp.upay.pipeline.domain.wechat.WechatCertificate;
import com.diligrp.upay.pipeline.domain.wechat.WechatConfig;
import com.diligrp.upay.pipeline.domain.wechat.AuthorizationSession;
import com.diligrp.upay.pipeline.domain.wechat.CertificateResponse;
import com.diligrp.upay.pipeline.domain.wechat.UploadShippingRequest;
import com.diligrp.upay.pipeline.domain.wechat.WechatAccessToken;
import com.diligrp.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.pipeline.util.WechatSignatureUtils;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.service.ServiceEndpointSupport;
import com.diligrp.upay.shared.util.DateUtils;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.shared.util.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.*;

/**
 * 微信支付基础功能HTTP客户端
 */
public class WechatHttpClient extends ServiceEndpointSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WechatHttpClient.class);

    // 获取微信平台证书列表
    private static final String LIST_CERTIFICATE = "/v3/certificates";

    private static final String WECHAT_BASE_URL = "https://api.weixin.qq.com";

    private static final String CODE_TO_SESSION = "/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/stable_token";

    private static final String UPLOAD_SHIPPING_URL = "https://api.weixin.qq.com/wxa/sec/order/upload_shipping_info";

    protected String wechatBaseUri;

    protected WechatConfig wechatConfig;

    public WechatHttpClient(String wechatBaseUri, WechatConfig wechatConfig) {
        this.wechatBaseUri = wechatBaseUri;
        this.wechatConfig = wechatConfig;
    }

    /**
     * Native支付下单, 返回二维码链接
     */
    public String sendNativePrepayRequest(WechatPrepayRequest request, String notifyUri) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持Native支付");
    }

    public String sendJsApiPrepayRequest(WechatPrepayRequest request, String notifyUri) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持JsApi支付");
    }

    public WechatPaymentResponse queryPrepayResponse(WechatPrepayOrder request) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持此操作");
    }

    public void closePrepayOrder(WechatPrepayOrder request) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持此操作");
    }

    public WechatRefundResponse sendRefundRequest(WechatRefundRequest request, String notifyUri) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持此操作");
    }

    public WechatRefundResponse queryRefundOrder(WechatRefundOrder request) throws Exception {
        throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "支付通道不支持此操作");
    }

    /**
     * 刷新微信支付平台数字证书(公钥)
     * 微信支付平台证书 - 当旧证书即将过期时，微信将新老证书将并行使用
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/wechatpay5_1.shtml">...</a>
     * <a href="https://pay.weixin.qq.com/docs/merchant/apis/platform-certificate/api-v3-get-certificates/get.html">...</a>
     */
    public void refreshCertificates() throws Exception {
        // 获取认证信息和签名信息
        String authorization = WechatSignatureUtils.authorization(wechatConfig.getMchId(), WechatConstants.HTTP_GET,
            LIST_CERTIFICATE, wechatConfig.getPrivateKey(), wechatConfig.getSerialNo());

        HttpRequest.Builder request = HttpRequest.newBuilder().uri(URI.create(wechatBaseUri + LIST_CERTIFICATE))
            .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(MAX_REQUEST_TIMEOUT_TIME))
            .header(CONTENT_TYPE, CONTENT_TYPE_JSON).header(WechatConstants.HEADER_AUTHORIZATION, authorization)
            .header(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON)
            .header(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        LOG.info("Sending wechat list certificate request...");
        LOG.debug("Authorization: {}\n", authorization);
        ServiceEndpointSupport.HttpResult result = execute(request.GET().build());
        if (result.statusCode == 200) { // 200 处理成功有返回，204处理成功无返回; 返回成功时再进行数据验签，对数据无安全隐患
            // 获取验签使用的微信平台证书序列号, 本地获取平台证书到则进行验签, 如获取不到则说明旧证书即将过期，新老证书正并行使用
            String serialNo = result.header(WechatConstants.HEADER_SERIAL_NO);
            String timestamp = result.header(WechatConstants.HEADER_TIMESTAMP);
            String nonce = result.header(WechatConstants.HEADER_NONCE);
            String sign = result.header(WechatConstants.HEADER_SIGNATURE);
            LOG.debug("\n------Wechat Platform Data Verify------\nWechatpay-Serial={}\nWechatpay-Timestamp={}\n" +
                "Wechatpay-Nonce={}\nWechatpay-Signature={}\n--------------------------------------", serialNo, timestamp, nonce, sign);
            LOG.debug(result.responseText);
            Optional<WechatCertificate> certificate = wechatConfig.getCertificate(serialNo);
            if (certificate.isPresent()) {
                boolean success = WechatSignatureUtils.verify(result.responseText, timestamp, nonce, sign,
                    certificate.get().getPublicKey());
                if (!success) {
                    throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "获取微信支付平台证书验签失败");
                }
            } else {
                LOG.warn("Old certificate is about to expire, new one is in use");
            }

            CertificateResponse response = JsonUtils.fromJsonString(result.responseText, CertificateResponse.class);
            List<CertificateResponse.Certificate> certificates = response.getData();
            if (ObjectUtils.isNotEmpty(certificates)) {
                for (CertificateResponse.Certificate cert : certificates) {
                    // 利用ApiV3Key解密平台公钥
                    String certStr = WechatSignatureUtils.decrypt(cert.getEncrypt_certificate().getCiphertext(),
                        cert.getEncrypt_certificate().getNonce(), cert.getEncrypt_certificate().getAssociated_data(),
                        wechatConfig.getApiV3Key());
                    ByteArrayInputStream is = new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8));
                    Certificate x509Cert = CertificateFactory.getInstance("X509").generateCertificate(is);
                    wechatConfig.putCertificate(WechatCertificate.of(cert.getSerial_no(), x509Cert.getPublicKey()));
                    LOG.info("{} certificate added", cert.getSerial_no());
                }
            }
            LOG.info("Refresh certificate repository success");
        } else {
            LOG.info("Refresh certificate repository failed: {}", result.statusCode);
        }
    }

    public String loginAuthorization(String code) {
        return loginAuthorization(wechatConfig.getAppId(), wechatConfig.getAppSecret(), code);
    }

    /**
     * 小程序登录授权，根据wx.login获得的临时登录凭证code，获取登录信息openId等
     */
    public String loginAuthorization(String appId, String appSecret, String code) {
        String uri = String.format(CODE_TO_SESSION, appId, appSecret, code);
        HttpRequest.Builder request = HttpRequest.newBuilder().uri(URI.create(WECHAT_BASE_URL + uri))
            .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(MAX_REQUEST_TIMEOUT_TIME))
            .header(CONTENT_TYPE, CONTENT_TYPE_JSON).header(WechatConstants.HEADER_ACCEPT, WechatConstants.ACCEPT_JSON)
            .header(WechatConstants.HEADER_USER_AGENT, WechatConstants.USER_AGENT);
        LOG.info("Requesting MiniPro login authorization info: {}\n{}", code, uri);
        HttpResult result = execute(request.GET().build());
        if (result.statusCode == 200) {
            LOG.debug("MiniPro login authorization info response\n{}", result.responseText);
            AuthorizationSession session = JsonUtils.fromJsonString(result.responseText, AuthorizationSession.class);
            if (session.getErrcode() != null && session.getErrcode() != 0) {
                LOG.error("Request MiniPro login authorization info failed: {}", session.getErrmsg());
                throw new PaymentPipelineException(ErrorCode.SERVICE_ACCESS_FAILED, "获取小程序登录授权信息失败: " + session.getErrmsg());
            }
            return session.getOpenid();
        } else {
            LOG.error("Request MiniPro login authorization info failed: {}", result.statusCode);
            throw new PaymentPipelineException(ErrorCode.SERVICE_ACCESS_FAILED, "获取小程序登录授权信息失败");
        }
    }

    public WechatAccessToken getAccessToken() {
        return getAccessToken(wechatConfig.getAppId(), wechatConfig.getAppSecret());
    }

    /**
     * 获取小程序接口调用凭证：Token有效期内重复调用该接口不会更新Token，有效期5分钟前更新Token，新旧Token并行5分钟；该接口调用频率限制为 1万次每分钟，每天限制调用 50万次；
     * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getStableAccessToken.html">...</a>
     */
    public WechatAccessToken getAccessToken(String appId, String appSecret) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", appId);
        params.put("secret", appSecret);
        params.put("force_refresh", Boolean.FALSE);
        String payload = JsonUtils.toJsonString(params);
        LOG.debug("Request MiniPro Api access token: {}", payload);
        HttpResult result = send(ACCESS_TOKEN_URL, payload);
        if (result.statusCode == 200) {
            LOG.debug("MiniPro Api access token response: {}", result.responseText);
            Map<String, Object> data = JsonUtils.fromJsonString(result.responseText, new TypeReference<>() {});
            return WechatAccessToken.of((String)data.get("access_token"), (Integer) data.get("expires_in"));
        } else {
            LOG.error("Request MiniPro Api access token failed: {}", result.statusCode);
            throw new PaymentPipelineException(ErrorCode.SERVICE_ACCESS_FAILED, "获取微信接口调用凭证失败");
        }
    }

    /**
     * 微信发货信息录入接口
     * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/business-capabilities/order-shipping/order-shipping.html">...</a>
     */
    public void sendUploadShippingRequest(UploadShippingRequest request, String accessToken) {
        String uri = String.format("%s?access_token=%s", UPLOAD_SHIPPING_URL, accessToken);
        Map<String, Object> orderKey = new HashMap<>();
        orderKey.put("order_number_type", 2);
        orderKey.put("transaction_id", request.getTransactionId());

        Map<String, Object> shipping = new HashMap<>();
        shipping.put("item_desc", request.getGoods());
        List<Map<String, Object>> shippingList = new ArrayList<>();
        shippingList.add(shipping);

        Map<String, Object> payer = new HashMap<>();
        payer.put("openid", request.getOpenId());

        Map<String, Object> params = new HashMap<>();
        params.put("order_key", orderKey);
        params.put("logistics_type", request.getLogisticsType());
        params.put("delivery_mode", 1);
        params.put("shipping_list", shippingList);
        params.put("upload_time", DateUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ssZ"));
        params.put("payer", payer);

        String payload = JsonUtils.toJsonString(params);
        LOG.debug("Request wechat upload shipping: {}", payload);
        HttpResult result = send(uri, payload);
        if (result.statusCode == 200) {
            LOG.debug("Wechat upload shipping response: {}", result.responseText);
            Map<String, Object> data = JsonUtils.fromJsonString(result.responseText, new TypeReference<>() {});
            int errorCode = (Integer) data.get("errcode");
            if (errorCode != 0) {
                String errorMessage = (String)data.get("errmsg");
                throw new PaymentPipelineException(ErrorCode.SERVICE_ACCESS_FAILED, "微信发货信息录入失败: " + errorMessage);
            }
        } else {
            LOG.error("Request wechat upload shipping failed: {}", result.statusCode);
            throw new PaymentPipelineException(ErrorCode.SERVICE_ACCESS_FAILED, "调用微信发货信息录入接口失败");
        }
    }

    /**
     * 用于微信支付结果通知数据验签
     */
    public boolean dataVerify(String serialNo, String timestamp, String nonce, String sign, String payload) throws Exception {
        LOG.debug("\n------Wechat Platform Data Verify------\nWechatpay-Serial={}\nWechatpay-Timestamp={}\n"
            + "Wechatpay-Nonce={}\nWechatpay-Signature={}\n{}\n--------------------------------------",
            serialNo, timestamp, nonce, sign, payload == null ? "" : payload);
        Optional<WechatCertificate> certOpt = wechatConfig.getCertificate(serialNo);
        if (certOpt.isEmpty()) { // 找不到证书则重新向微信平台请求新证书(旧证书即将过期时出现)
            refreshCertificates();
        }

        WechatCertificate certificate = wechatConfig.getCertificate(serialNo).orElseThrow(() ->
            new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "找不到微信平台数字证书"));
        return WechatSignatureUtils.verify(payload, timestamp, nonce, sign, certificate.getPublicKey());
    }

    protected void verifyHttpResult(HttpResult result) throws Exception {
        if (result.statusCode == 401) {
            return; // 微信签名失败，则不进行验签操作
        }
        // 获取验签使用的微信平台证书序列号, 本地获取平台证书到则进行验签, 如获取不到则说明旧证书即将过期，新老证书正并行使用
        String serialNo = result.header(WechatConstants.HEADER_SERIAL_NO);
        String timestamp = result.header(WechatConstants.HEADER_TIMESTAMP);
        String nonce = result.header(WechatConstants.HEADER_NONCE);
        String sign = result.header(WechatConstants.HEADER_SIGNATURE);
        if (!dataVerify(serialNo, timestamp, nonce, sign, result.responseText)) {
            throw new PaymentPipelineException(ErrorCode.OPERATION_NOT_ALLOWED, "微信数据验签失败");
        }
    }

    public WechatConfig getWechatConfig() {
        return wechatConfig;
    }
}
