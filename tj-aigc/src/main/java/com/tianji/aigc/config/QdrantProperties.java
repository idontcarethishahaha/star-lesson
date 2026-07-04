package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.qdrant")
public class QdrantProperties {

    private String host;
    private int port = 6333;
    private int grpcPort = 6334;
    private boolean secure;
}
