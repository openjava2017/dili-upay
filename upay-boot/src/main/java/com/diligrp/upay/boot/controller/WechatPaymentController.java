package com.diligrp.upay.boot.controller;

import com.diligrp.upay.boot.domain.wechat.DirectTransactionResponse;
import com.diligrp.upay.boot.domain.wechat.NotifyRefundResponse;
import com.diligrp.upay.boot.domain.wechat.PartnerTransactionResponse;
import com.diligrp.upay.boot.domain.wechat.WechatNotifyResponse;
import com.diligrp.upay.boot.util.HttpUtils;
import com.diligrp.upay.pipeline.core.WechatPipeline;
import com.diligrp.upay.pipeline.domain.WechatPaymentResponse;
import com.diligrp.upay.pipeline.domain.WechatRefundResponse;
import com.diligrp.upay.pipeline.impl.WechatPartnerPipeline;
import com.diligrp.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.upay.pipeline.util.WechatConstants;
import com.diligrp.upay.pipeline.util.WechatSignatureUtils;
import com.diligrp.upay.shared.util.DateUtils;
import com.diligrp.upay.shared.util.JsonUtils;
import com.diligrp.upay.trade.service.IWechatPaymentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 微信支付后台控制器
 */
@RestController
@RequestMapping("/wechat")
public class WechatPaymentController {

    private static final Logger LOG = LoggerFactory.getLogger(WechatPaymentController.class);

    @Resource
    private IWechatPaymentService wechatPaymentService;

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    /**
     * 杭州果品微信支付结果通知-服务商模式
     */
    @RequestMapping(value = "/payment/notify.do")
    public ResponseEntity<NotifyResult> paymentNotify(HttpServletRequest request) {
        LOG.info("Receiving wechat payment result notify......");
        String payload = HttpUtils.httpBody(request);

        try {
            Long pipelineId = Long.parseLong(request.getParameter(WechatConstants.PARAM_PIPELINE));
            WechatPipeline pipeline = paymentPipelineManager.findPipelineById(pipelineId, WechatPipeline.class);
            if (dataVerify(request, pipeline, payload)) {
                WechatNotifyResponse response = JsonUtils.fromJsonString(payload, WechatNotifyResponse.class);
                WechatPaymentResponse paymentResponse = paymentResponse(pipeline, response);
                if (WechatConstants.NOTIFY_EVENT_TYPE.equals(response.getEvent_type())) {
                    wechatPaymentService.notifyPaymentResult(paymentResponse);
                }
                return ResponseEntity.ok(NotifyResult.success());
            } else {
                LOG.error("Wechat payment result notify data verify failed");
                return ResponseEntity.badRequest().body(NotifyResult.failure("Data verify failed"));
            }
        } catch (Exception ex) {
            LOG.error("Process wechat payment result notify exception", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(NotifyResult.failure("Process wechat payment result notify exception"));
        }
    }

    @RequestMapping(value = "/refund/notify.do")
    public ResponseEntity<NotifyResult> refundNotify(HttpServletRequest request) {
        LOG.info("Receiving wechat refund result notify......");
        String payload = HttpUtils.httpBody(request);

        try {
            Long pipelineId = Long.parseLong(request.getParameter(WechatConstants.PARAM_PIPELINE));
            WechatPipeline pipeline = paymentPipelineManager.findPipelineById(pipelineId, WechatPipeline.class);

            if (dataVerify(request, pipeline, payload)) {
                WechatNotifyResponse notifyResponse = JsonUtils.fromJsonString(payload, WechatNotifyResponse.class);
                if (WechatConstants.REFUND_EVENT_TYPE.equals(notifyResponse.getEvent_type())) {
                    WechatRefundResponse refundResponse = refundResponse(pipeline, notifyResponse);
                    wechatPaymentService.notifyRefundResult(refundResponse);
                }
                return ResponseEntity.ok(NotifyResult.success());
            } else {
                LOG.error("Wechat refund result notify data verify failed");
                return ResponseEntity.badRequest().body(NotifyResult.failure("Data verify failed"));
            }
        } catch (Exception ex) {
            LOG.error("Process wechat refund result notify exception", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(NotifyResult.failure("Process wechat refund result notify exception"));
        }
    }

    private boolean dataVerify(HttpServletRequest request, WechatPipeline pipeline, String payload) {
        String serialNo = request.getHeader(WechatConstants.HEADER_SERIAL_NO);
        String timestamp = request.getHeader(WechatConstants.HEADER_TIMESTAMP);
        String nonce = request.getHeader(WechatConstants.HEADER_NONCE);
        String sign = request.getHeader(WechatConstants.HEADER_SIGNATURE);

        try {
            return pipeline.getClient().dataVerify(serialNo, timestamp, nonce, sign, payload);
        } catch (Exception ex) {
            LOG.error("Wechat result notify data verify failed", ex);
            return false;
        }
    }

    private WechatPaymentResponse paymentResponse(WechatPipeline pipeline, WechatNotifyResponse notifyResponse) throws Exception {
        WechatNotifyResponse.Resource resource = notifyResponse.getResource();
        String payload = WechatSignatureUtils.decrypt(resource.getCiphertext(), resource.getNonce(),
            resource.getAssociated_data(), pipeline.getClient().getWechatConfig().getApiV3Key());
        if (pipeline instanceof WechatPartnerPipeline) {
            PartnerTransactionResponse response = JsonUtils.fromJsonString(payload, PartnerTransactionResponse.class);
            LocalDateTime when = DateUtils.parseDateTime(response.getSuccess_time(), WechatConstants.RFC3339_FORMAT);
            String payer = response.getPayer() == null ? null : response.getPayer().getSp_openid();
            return WechatPaymentResponse.of(response.getOut_trade_no(), response.getTransaction_id(), payer, when,
                response.getTrade_state(), response.getTrade_state_desc());
        } else {
            DirectTransactionResponse response = JsonUtils.fromJsonString(payload, DirectTransactionResponse.class);
            LocalDateTime when = DateUtils.parseDateTime(response.getSuccess_time(), WechatConstants.RFC3339_FORMAT);
            String payer = response.getPayer() == null ? null : response.getPayer().getOpenid();
            return WechatPaymentResponse.of(response.getOut_trade_no(), response.getTransaction_id(), payer, when,
                response.getTrade_state(), response.getTrade_state_desc());
        }
    }

    private WechatRefundResponse refundResponse(WechatPipeline pipeline, WechatNotifyResponse notifyResponse) throws Exception {
        WechatNotifyResponse.Resource resource = notifyResponse.getResource();
        String payload = WechatSignatureUtils.decrypt(resource.getCiphertext(), resource.getNonce(),
            resource.getAssociated_data(), pipeline.getClient().getWechatConfig().getApiV3Key());
        NotifyRefundResponse response = JsonUtils.fromJsonString(payload, NotifyRefundResponse.class);
        LocalDateTime when = DateUtils.parseDateTime(response.getSuccess_time(), WechatConstants.RFC3339_FORMAT);
        return WechatRefundResponse.of(response.getRefund_id(), response.getOut_refund_no(), when,
            response.getRefund_status(), response.getRefund_status());
    }

    private static class NotifyResult {
        private String code;
        private String message;

        public static NotifyResult success() {
            NotifyResult result = new NotifyResult();
            result.code = "SUCCESS";
            result.message = "SUCCESS";
            return result;
        }

        public static NotifyResult failure(String message) {
            NotifyResult result = new NotifyResult();
            result.code = "FAILED";
            result.message = message;
            return result;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
