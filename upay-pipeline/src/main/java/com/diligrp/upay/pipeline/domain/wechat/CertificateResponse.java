package com.diligrp.upay.pipeline.domain.wechat;

import java.util.List;

/**
 * 微信支付平台证书响应接口模型
 */
public class CertificateResponse {
    // 证书列表
    private List<Certificate> data;

    public List<Certificate> getData() {
        return data;
    }

    public void setData(List<Certificate> data) {
        this.data = data;
    }

    public static class Certificate {
        // 证书序列号
        private String serial_no;
        // 证书有效时间
        private String effective_time;
        // 证书过期时间
        private String expire_time;
        // 证书内容
        private EncryptCertificate encrypt_certificate;

        public String getSerial_no() {
            return serial_no;
        }

        public void setSerial_no(String serial_no) {
            this.serial_no = serial_no;
        }

        public String getEffective_time() {
            return effective_time;
        }

        public void setEffective_time(String effective_time) {
            this.effective_time = effective_time;
        }

        public String getExpire_time() {
            return expire_time;
        }

        public void setExpire_time(String expire_time) {
            this.expire_time = expire_time;
        }

        public EncryptCertificate getEncrypt_certificate() {
            return encrypt_certificate;
        }

        public void setEncrypt_certificate(EncryptCertificate encrypt_certificate) {
            this.encrypt_certificate = encrypt_certificate;
        }

        public static class EncryptCertificate {
            // 证书加密算法
            private String algorithm;
            // 随机字符串
            private String nonce;
            // 附加数据
            private String associated_data;
            // 证书密文
            private String ciphertext;

            public String getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(String algorithm) {
                this.algorithm = algorithm;
            }

            public String getNonce() {
                return nonce;
            }

            public void setNonce(String nonce) {
                this.nonce = nonce;
            }

            public String getAssociated_data() {
                return associated_data;
            }

            public void setAssociated_data(String associated_data) {
                this.associated_data = associated_data;
            }

            public String getCiphertext() {
                return ciphertext;
            }

            public void setCiphertext(String ciphertext) {
                this.ciphertext = ciphertext;
            }
        }
    }
}
