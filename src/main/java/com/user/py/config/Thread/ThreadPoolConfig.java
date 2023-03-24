package com.user.py.config.Thread;


import com.user.py.utils.Threads;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ice
 * @date 2022/9/11 17:28
 */
@Configuration
public class ThreadPoolConfig {
    // 核心线程池大小
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    // 最大可创建的线程数
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    // 队列最大长度
    private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    private int keepAliveTime = 300;


    @Bean(name = "executorService")
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(corePoolSize,maxPoolSize
                , keepAliveTime,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity)){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
