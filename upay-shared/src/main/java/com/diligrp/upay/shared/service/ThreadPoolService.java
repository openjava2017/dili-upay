package com.diligrp.upay.shared.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 请谨慎使用此线程池工具类，通常建议根据特定的使用场景设置线程池参数，不建议使用统一的线程池配置
 * JDK的线程池类并不能很好区分"计算密集型"和"IO密集型"任务类型，并根据不同的任务类型去配置不同的参数
 */
public final class ThreadPoolService {

    private static final int CPU_CORE_NUM = Runtime.getRuntime().availableProcessors();

    private static final int CPU_MAX_POOL_SIZE = 100;

    private static final int IO_MAX_POOL_SIZE = 1000;

    // CPU运算密集型任务的线程池实例
    private static volatile ExecutorService cpuThreadPoll;

    // IO密集型任务的线程池实例
    private static volatile ExecutorService ioThreadPoll;

    private ThreadPoolService() {
    }

    /**
     * 获取运算密集型任务的线程池实例
     * 通常建议根据特定的使用场景设置线程池参数，不建议使用统一的线程池配置
     */
    public static ExecutorService getCpuThreadPoll() {
        if (cpuThreadPoll == null) {
            synchronized (ThreadPoolService.class) {
                if (cpuThreadPoll == null) {
                    cpuThreadPoll = new ThreadPoolExecutor(CPU_CORE_NUM + 1, CPU_MAX_POOL_SIZE,
                        20, TimeUnit.SECONDS, new LinkedBlockingQueue(100),
                        new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return cpuThreadPoll;
    }

    /**
     * 获取IO密集型任务的线程池实例
     * 通常建议根据特定的使用场景设置线程池参数，不建议使用统一的线程池配置
     */
    public static ExecutorService getIoThreadPoll() {
        if (ioThreadPoll == null) {
            synchronized (ThreadPoolService.class) {
                if (ioThreadPoll == null) {
                    ioThreadPoll = new ThreadPoolExecutor(CPU_CORE_NUM + 1, IO_MAX_POOL_SIZE,
                        20, TimeUnit.SECONDS, new LinkedBlockingQueue(1000),
                        new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return ioThreadPoll;
    }
}
