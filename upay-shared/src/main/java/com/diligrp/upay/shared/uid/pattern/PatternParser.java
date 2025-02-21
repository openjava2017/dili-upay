package com.diligrp.upay.shared.uid.pattern;

import com.diligrp.upay.shared.ErrorCode;
import com.diligrp.upay.shared.exception.PlatformServiceException;

import java.util.ArrayList;
import java.util.List;

public class PatternParser {
    private final String pattern;
    private final int length;
    private TokenizerState state;
    private int index;

    public PatternParser(String pattern) {
        this.pattern = pattern;
        this.length = pattern.length();
        this.index = 0;
        this.state = TokenizerState.LITERAL_STATE;
    }

    public List<Token> parse() {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        while(index < length) {
            char c = pattern.charAt(index);
            index ++;

            switch (this.state) {
                case LITERAL_STATE:
                    handleLiteralState(c, tokens, buf);
                    break;
                case KEYWORD_STATE:
                    handleKeywordState(c, tokens, buf);
                    break;
                case OPTION_STATE:
                    handleOptionState(c, tokens, buf);
                    break;
            }
        }

        switch (state) {
            case LITERAL_STATE:
                handleLiteralState('%', tokens, buf);
                break;
            case KEYWORD_STATE:
                handleKeywordState('%', tokens, buf);
                break;
            default:
                throw new PlatformServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "Unexpected end of pattern string");
        }

        return tokens;
    }

    private void handleLiteralState(char c, List<Token> tokens, StringBuilder buf) {
        switch (c) {
            case '%':
                addLiteralToken(buf, tokens);
                state = TokenizerState.KEYWORD_STATE;
                break;
            default:
                buf.append(c);
        }
    }

    private void handleKeywordState(char c, List<Token> tokens, StringBuilder buf) {
        switch (c) {
            case '%':
                addKeywordToken(buf, tokens);
                state = TokenizerState.KEYWORD_STATE;
                break;
            case '{':
                this.addKeywordToken(buf, tokens);
                this.state = TokenizerState.OPTION_STATE;
                break;
            default:
                buf.append(c);
        }
    }

    private void handleOptionState(char c, List<Token> tokens, StringBuilder buf) {
        switch (c) {
            case '}':
                addOptionToken(buf, tokens);
                state = TokenizerState.LITERAL_STATE;
                break;
            default:
                buf.append(c);
        }
    }

    private void addLiteralToken(StringBuilder buf, List<Token> tokens) {
        if (!buf.isEmpty()) {
            tokens.add(new LiteralToken(buf.toString()));
            buf.setLength(0);
        }
    }

    private void addKeywordToken(StringBuilder buf, List<Token> tokens) {
        if (!buf.isEmpty()) {
            tokens.add(new KeywordToken(buf.toString()));
            buf.setLength(0);
        }
    }

    private void addOptionToken(StringBuilder buf, List<Token> tokens) {
        if (!buf.isEmpty()) {
            tokens.add(new OptionToken(buf.toString()));
            buf.setLength(0);
        }
    }

    private enum TokenizerState {
        LITERAL_STATE,
        KEYWORD_STATE,
        OPTION_STATE
    }
}
