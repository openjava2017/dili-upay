package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.domain.SequenceKey;

public class LiteralConverter extends Converter<SequenceKey> {
    private final String literal;

    public LiteralConverter(String literal) {
        this.literal = literal;
    }

    @Override
    public String convert(SequenceKey context) {
        return literal;
    }
}
