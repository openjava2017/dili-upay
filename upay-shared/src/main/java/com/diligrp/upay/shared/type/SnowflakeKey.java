package com.diligrp.upay.shared.type;

import com.diligrp.upay.shared.uid.SnowflakeKeyManager;

public enum SnowflakeKey implements SnowflakeKeyManager.SnowflakeKey {
    TRADE_ID,

    PAYMENT_ID,

    PROTOCOL_ID
}
