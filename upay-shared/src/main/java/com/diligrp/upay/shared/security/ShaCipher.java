package com.diligrp.upay.shared.security;

import java.security.MessageDigest;

/**
 * SHA散列算法工具类
 */
public class ShaCipher {
    private static final String KEY_SHA = "SHA";

    public static byte[] encrypt(byte[] data) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(data);

        return sha.digest();
    }
}
