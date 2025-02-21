package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.SequenceKey;
import com.diligrp.upay.shared.exception.PlatformServiceException;

public class OptionToken extends Token {
    public OptionToken(String token) {
        super(token);
    }

    public String getToken() {
        return this.token;
    }

    @Override
    Converter<SequenceKey> getConverter() {
        throw new PlatformServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "Not supported converter");
    }

    public String toString() {
        return String.format("option(%s)", token);
    }
}
