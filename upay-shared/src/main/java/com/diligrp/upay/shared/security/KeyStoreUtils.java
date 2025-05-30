package com.diligrp.upay.shared.security;

import com.diligrp.upay.shared.util.ClassUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/**
 * 数字证书工具类
 */
public class KeyStoreUtils {
    public static String getPrivateKeyStr(String keyStorePath, String storeType, String storePass,
                                          String alias, String keyPass) throws Exception {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        PrivateKey privateKey = getPrivateKey(in, storeType, storePass, alias, keyPass);
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static PrivateKey getPrivateKey(String keyStorePath, String storeType, String storePass,
                                           String alias, String keyPass) throws Exception {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        return getPrivateKey(in, storeType, storePass, alias, keyPass);
    }

    public static PrivateKey getPrivateKey(InputStream in, String storeType, String storePass, String alias, String keyPass) throws Exception {
        KeyStore ks = getKeyStore(in, storeType, storePass);
        PrivateKey key = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
        return key;
    }

    public static String getPublicKeyStr(String keyStorePath, String storeType, String storePass, String alias) throws Exception {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        PublicKey publicKey = getPublicKey(in, storeType, storePass, alias);
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey getPublicKey(String keyStorePath, String storeType, String storePass, String alias) throws Exception {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        return getPublicKey(in, storeType, storePass, alias);
    }

    public static PublicKey getPublicKey(InputStream in, String storeType, String storePass, String alias) throws Exception {
        KeyStore ks = getKeyStore(in, storeType, storePass);
        Certificate cert = ks.getCertificate(alias);
        return cert.getPublicKey();
    }

    public static String getPublicKeyStr(String certificatePath) throws Exception {
        PublicKey publicKey = getPublicKey(certificatePath);
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey getPublicKey(String certificatePath) throws Exception {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(certificatePath);
        Certificate x509Cert = CertificateFactory.getInstance("X509").generateCertificate(in);
        return x509Cert.getPublicKey();
    }

    public static KeyStore getKeyStore(InputStream in, String storeType, String storePass) throws Exception {
        KeyStore ks = KeyStore.getInstance(storeType);
        ks.load(in, storePass.toCharArray());
        in.close();
        return ks;
    }
}
