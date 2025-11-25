package com.fsm.task.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for HTTP client beans.
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Creates a RestTemplate bean for making HTTP requests.
     * 
     * @return configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
