package com.hyw.apigateway.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @SuppressWarnings("UnstableApiUsage")
    @Bean
    public RateLimiter rateLimiter() {
        // 每秒限流5个请求
        return RateLimiter.create(5.0);
    }
}