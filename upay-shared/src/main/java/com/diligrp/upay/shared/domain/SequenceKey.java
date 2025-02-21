package com.diligrp.upay.shared.domain;

import java.time.LocalDate;

public class SequenceKey {
    private long sequence;
    private LocalDate when;

    public SequenceKey(long sequence, LocalDate when) {
        this.sequence = sequence;
        this.when = when;
    }

    public long getSequence() {
        return sequence;
    }

    public LocalDate getWhen() {
        return when;
    }
}
