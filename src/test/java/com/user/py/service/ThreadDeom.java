package com.user.py.service;

import com.user.py.utils.ThreadUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * @Author ice
 * @Date 2023/2/25 21:13
 * @Description: TODO
 */
public class ThreadDeom {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = ThreadUtil.getThreadPool();
        CompletableFuture<Integer> async = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return 0;
            }
            return 10;
        }, threadPool);
        System.out.println("======================");
        Integer integer = async.get();
        threadPool.shutdown();
        System.out.println(integer);
        System.out.println("------------------------------");
    }
}
