package com.diligrp.upay.shared.codec;

import java.nio.charset.StandardCharsets;

public class StringCodec {

    public static ByteEncoder<String> getEncoder() {
        return StringEncoder.INSTANCE;
    }

    public static ByteDecoder<String> getDecoder() {
        return StringDecoder.INSTANCE;
    }

    static class StringEncoder implements ByteEncoder<String> {

        static final ByteEncoder<String> INSTANCE = new StringEncoder();

        @Override
        public byte[] encode(String payload) {
            return payload.getBytes(StandardCharsets.UTF_8);
        }
    }

    static class StringDecoder implements ByteDecoder<String> {

        static final ByteDecoder<String> INSTANCE = new StringDecoder();

        @Override
        public String decode(byte[] payload) {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }
}
