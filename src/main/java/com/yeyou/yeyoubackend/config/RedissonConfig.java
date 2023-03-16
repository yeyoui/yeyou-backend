package com.yeyou.yeyoubackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spring.redis")
@Configuration
@Data
public class RedissonConfig {
    //load by yaml
    private String host;
    private String port;
    private int redissonDatabase;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(redissonDatabase);
        return Redisson.create(config);
    }
}
