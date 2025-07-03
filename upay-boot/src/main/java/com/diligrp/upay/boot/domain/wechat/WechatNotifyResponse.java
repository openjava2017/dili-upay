package com.diligrp.upay.boot.domain.wechat;

/**
 * 微信支付结果通知模型
 */
public class WechatNotifyResponse {
    // 通知ID
    private String id;
    // 通知类型
    private String event_type;
    // 通知数据类型
    private String resource_type;
    // 通知数据
    private Resource resource;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public static class Resource {
        // 加密算法类型 - AEAD_AES_256_GCM
        private String algorithm;
        // 数据密文
        private String ciphertext;
        // 附加数据
        private String associated_data;
        // 原始类型
        private String original_type;
        // 随机串
        private String nonce;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public String getAssociated_data() {
            return associated_data;
        }

        public void setAssociated_data(String associated_data) {
            this.associated_data = associated_data;
        }

        public String getOriginal_type() {
            return original_type;
        }

        public void setOriginal_type(String original_type) {
            this.original_type = original_type;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }
    }
}
