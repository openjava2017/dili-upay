package com.diligrp.upay.boot.controller;

import com.diligrp.upay.boot.util.Constants;
import com.diligrp.upay.boot.util.HttpUtils;
import com.diligrp.upay.core.domain.ApplicationPermit;
import com.diligrp.upay.core.domain.MerchantPermit;
import com.diligrp.upay.core.exception.PaymentServiceException;
import com.diligrp.upay.core.service.IAccessPermitService;
import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.Message;
import com.diligrp.upay.shared.domain.RequestContext;
import com.diligrp.upay.shared.exception.PlatformServiceException;
import com.diligrp.upay.shared.sapi.ICallableServiceManager;
import com.diligrp.upay.shared.util.AssertUtils;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.shared.util.ObjectUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付服务控制器
 */
@RestController
@RequestMapping("/payment/api")
public class PaymentPlatformController {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentPlatformController.class);

    @Resource
    private ICallableServiceManager callableServiceManager;

    @Resource
    private IAccessPermitService accessPermitService;

    @RequestMapping(value = "/gateway.do")
    public void gateway(HttpServletRequest request, HttpServletResponse response) {
        Message<?> result;
        String payload = HttpUtils.httpBody(request);
        RequestContext context = HttpUtils.requestContext(request);
        String service = context.getService();
        Long mchId = context.getLong(Constants.PARAM_MCHID);
        Long appId = context.getLong(Constants.PARAM_APPID);
        String token = context.getString(Constants.PARAM_TOKEN);

        try {
            LOG.debug("Payment request received for mchId: {}, service:{}\n{}", mchId, service, payload);
            AssertUtils.notNull(appId, "appId missed");
            AssertUtils.notNull(mchId, "mchId missed");
            AssertUtils.notEmpty(service, "service missed");
            AssertUtils.notEmpty(payload, "payment request payload missed");

            checkAccessPermission(context, mchId, appId, token);
            result = callableServiceManager.callService(context, payload);
        } catch (IllegalArgumentException iex) {
            LOG.error(iex.getMessage());
            result = Message.failure(ErrorCode.ILLEGAL_ARGUMENT_ERROR, iex.getMessage());
        } catch (PlatformServiceException pex) {
            LOG.error("Payment service process exception", pex);
            result = Message.failure(pex.getCode(), pex.getMessage());
        } catch (Throwable ex) {
            LOG.error("Payment service unknown exception", ex);
            result = Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, "支付系统未知异常，请联系系统管理员");
        }

        payload = JsonUtils.toJsonString(result);
        HttpUtils.sendResponse(response, payload);
    }

    @RequestMapping(value = "/boss.do")
    public void boss(HttpServletRequest request, HttpServletResponse response) {
        Message<?> result;
        String payload = HttpUtils.httpBody(request);
        RequestContext context = HttpUtils.requestContext(request);
        String service = context.getService();

        try {
            LOG.debug("Payment boss request received, service:{}\n{}", service, payload);
            AssertUtils.notEmpty(service, "service missed");
            AssertUtils.notEmpty(payload, "payment request payload missed");

            result = callableServiceManager.callService(context, payload);
        } catch (IllegalArgumentException iex) {
            LOG.error(iex.getMessage());
            result = Message.failure(ErrorCode.ILLEGAL_ARGUMENT_ERROR, iex.getMessage());
        } catch (PlatformServiceException pex) {
            LOG.error("Payment service process exception", pex);
            result = Message.failure(pex.getCode(), pex.getMessage());
        } catch (Throwable ex) {
            LOG.error("Payment service unknown exception", ex);
            result = Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, "支付系统未知异常，请联系系统管理员");
        }

        payload = JsonUtils.toJsonString(result);
        HttpUtils.sendResponse(response, payload);
    }

    /**
     * 检查接口访问权限，验证应用token
     */
    private void checkAccessPermission(RequestContext context, Long mchId, Long appId, String token) {
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(mchId);
        ApplicationPermit application = accessPermitService.loadApplicationPermit(appId);

        // 校验应用访问权限, 暂时不校验商户状态
        if (!ObjectUtils.equals(token, application.getToken())) {
            throw new PaymentServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "未授权的服务访问");
        }
        application.setMerchant(merchant);
        context.put(ApplicationPermit.class.getName(), application);
    }
}