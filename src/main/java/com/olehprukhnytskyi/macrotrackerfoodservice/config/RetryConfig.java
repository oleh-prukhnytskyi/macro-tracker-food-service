package com.olehprukhnytskyi.macrotrackerfoodservice.config;

import com.mongodb.DuplicateKeyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {
    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .fixedBackoff(100)
                .retryOn(DuplicateKeyException.class)
                .build();
    }
}
