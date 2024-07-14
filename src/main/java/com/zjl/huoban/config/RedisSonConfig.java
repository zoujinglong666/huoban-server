package com.zjl.huoban.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zou
 */
@Configuration
public class RedisSonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = "redis://127.0.0.1:6379";
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        return Redisson.create(config);
    }
}
