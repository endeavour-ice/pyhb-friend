package com.user.py;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author ice
 * @Date 2022/10/9 16:17
 * @PackageName:com.user.py
 * @ClassName: ServiceMain
 * @Description: TODO
 * @Version 1.0
 */
@SpringBootApplication
@EnableScheduling // 开启定时任务
@MapperScan("com.user.py.mapper")
@ComponentScan("com.user")
public class ServiceMainApplication {
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(ServiceMainApplication.class, args);
    }
}
