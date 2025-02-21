package com.diligrp.upay.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分表策略
 */
public class DataPartition {

    private static Map<Long, DataPartition> strategies = new ConcurrentHashMap<>();

    static {
        register(DataPartition.of(2L, "2"));
    }

    // 商户ID
    private Long mchId;
    private String partition;

    public String getPartition() {
        return partition;
    }

    public static DataPartition strategy(Long mchId) {
        DataPartition partition = strategies.get(mchId);
        return partition != null ? partition : DataPartition.of(mchId, "");
    }

    private static DataPartition of(Long mchId, String partition) {
        DataPartition dataPartition = new DataPartition();
        dataPartition.mchId = mchId;
        dataPartition.partition = partition;
        return dataPartition;
    }

    private static void register(DataPartition partition) {
        strategies.put(partition.mchId, partition);
    }
}
