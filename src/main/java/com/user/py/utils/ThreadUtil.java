package com.user.py.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ice
 * @date 2022/9/11 17:28
 */

public class ThreadUtil {

    private static final ExecutorService executor= new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors()+1, 5000,
            TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    public static ExecutorService getThreadPool() {
        return executor;
    }
}
