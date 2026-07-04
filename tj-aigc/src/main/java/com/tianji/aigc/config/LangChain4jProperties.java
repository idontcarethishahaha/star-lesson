package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j")
public class LangChain4jProperties {

    private String baseUrl;
    private String apiKey;
    private String embeddingModelName;
    private String modelName;
    private Integer maxTokens;
    private Integer timeoutSeconds;
    private Integer maxRetries;
    private Double chatModelTemperature;
}