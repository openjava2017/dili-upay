package com.diligrp.upay.shared.security;

import java.security.MessageDigest;

/**
 * MD5算法工具类
 */
public class Md5Cipher {
    private static final String KEY_MD5 = "MD5";

    public static byte[] encrypt(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(data);
        return md5.digest();
    }
}
