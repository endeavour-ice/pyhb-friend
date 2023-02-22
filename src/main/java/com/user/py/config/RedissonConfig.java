package com.user.py.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author ice
 * @date 2022/8/20 16:22
 */
@Configuration
@ConfigurationProperties("spring.redis")
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
@Data
public class RedissonConfig {
    private String password;
    private String host;
    private String port;

    @Bean
    public RedissonClient redissonClient() {
        // 1. Create config object
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setDatabase(3).setPassword(password).setAddress(redisAddress);
        return Redisson.create(config);
    }
    @Bean
    public static ConfigureRedisAction configureRedisAction(){
        return ConfigureRedisAction.NO_OP;
    }
}
