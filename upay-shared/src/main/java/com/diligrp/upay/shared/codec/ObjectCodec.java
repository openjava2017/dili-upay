package com.diligrp.upay.shared.codec;

import java.io.*;

public class ObjectCodec {

    public static ByteEncoder<Object> getEncoder() {
        return ObjectEncoder.INSTANCE;
    }

    public static ByteDecoder<Object> getDecoder() {
        return ObjectDecoder.INSTANCE;
    }

    static class ObjectEncoder implements ByteEncoder<Object> {

        static final ByteEncoder<Object> INSTANCE = new ObjectEncoder();

        @Override
        public byte[] encode(Object payload) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(buffer);
            os.writeObject(payload);
            os.close();
            return buffer.toByteArray();
        }
    }

    static class ObjectDecoder implements ByteDecoder<Object> {

        static final ByteDecoder<Object> INSTANCE = new ObjectDecoder();

        @Override
        public Object decode(byte[] payload) throws IOException {
            try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(payload))) {
                return is.readObject();
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
    }
}
