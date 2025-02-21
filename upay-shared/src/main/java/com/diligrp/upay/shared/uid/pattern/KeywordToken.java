package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.SequenceKey;
import com.diligrp.upay.shared.exception.PlatformServiceException;

public class KeywordToken extends Token {
    public KeywordToken(String token) {
        super(token);
    }

    @Override
    Converter<SequenceKey> getConverter() {
        if ("d".equals(token) || "date".equals(token)) {
            return new DateConverter(option);
        } else if ("n".equals(token)) {
            return new SequenceConverter(option);
        } else if ("r".equals(token)) {
            return new RandomConverter(option);
        } else {
            throw new PlatformServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "Unrecognized keyword " + token);
        }
    }

    public String toString() {
        return String.format("keyword(%s)", token);
    }
}
