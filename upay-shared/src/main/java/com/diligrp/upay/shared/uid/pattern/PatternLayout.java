package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.domain.SequenceKey;

import java.util.ArrayList;
import java.util.List;

public class PatternLayout {

    private final Converter<SequenceKey> head;

    public PatternLayout(String pattern) {
        this.head = compile(pattern);
    }

    public String doLayout(SequenceKey context) {
        StringBuilder writer = new StringBuilder();
        Converter<SequenceKey> converter = head;

        while (converter != null) {
            writer.append(converter.convert(context));
            converter = converter.getNext();
        }
        return writer.toString();
    }

    private Converter<SequenceKey> compile(String pattern) {
        PatternParser parser = new PatternParser(pattern);
        List<Token> tokens = new ArrayList<>();
        Token previous = null;
        for (Token token : parser.parse()) {
            if (token instanceof KeywordToken || token instanceof LiteralToken) {
                previous = token;
                tokens.add(token);
            } else if (token instanceof OptionToken) {
                if (previous != null) {
                    previous.setOption(((OptionToken) token).getToken());
                }
            }
        }

        Converter<SequenceKey> first = tokens.get(0).getConverter();
        Converter<SequenceKey> current = first;
        for (int i = 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            current.setNext(token.getConverter());
            current = current.getNext();
        }

        return first;
    }
}
