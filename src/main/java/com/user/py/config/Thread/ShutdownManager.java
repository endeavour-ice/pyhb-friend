package com.user.py.config.Thread;

import com.user.py.utils.Threads;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class ShutdownManager {
    @Resource
    private ExecutorService executorService;

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager() {
        try {
            log.info("====关闭后台任务任务线程池====");
            Threads.shutdownAndAwaitTermination(executorService);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}