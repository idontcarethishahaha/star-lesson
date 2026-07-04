package com.tianji.aigc.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 * 提供 EmbeddingModel（向量模型）等 Bean
 *
 * @author lusy
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LangChain4jConfig {

    private final LangChain4jProperties langChain4jProperties;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("[LangChain4jConfig] 初始化 EmbeddingModel, baseUrl={}, modelName={}",
                langChain4jProperties.getBaseUrl(),
                langChain4jProperties.getEmbeddingModelName());
        return OpenAiEmbeddingModel.builder()
                .baseUrl(langChain4jProperties.getBaseUrl())
                .apiKey(langChain4jProperties.getApiKey())
                .modelName(langChain4jProperties.getEmbeddingModelName())
                .build();
    }
}