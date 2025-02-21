package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.domain.SequenceKey;
import com.diligrp.upay.shared.exception.PlatformServiceException;
import com.diligrp.upay.shared.util.RandomUtils;

public class RandomConverter extends Converter<SequenceKey> {
    private static final int DEFAULT_LENGTH = 1;

    private final int length;

    public RandomConverter(String length) {
        if (length != null) {
            try {
                this.length = Integer.parseInt(length);
            } catch (Exception ex) {
                throw new PlatformServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "Invalid length for %r token");
            }
        } else {
            this.length = DEFAULT_LENGTH;
        }
    }

    @Override
    public String convert(SequenceKey context) {
        return RandomUtils.randomNumber(length);
    }
}
