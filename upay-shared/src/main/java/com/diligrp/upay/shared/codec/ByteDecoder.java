package com.diligrp.upay.shared.codec;

import java.io.IOException;

public interface ByteDecoder<T> {
    T decode(byte[] payload) throws IOException;
}
