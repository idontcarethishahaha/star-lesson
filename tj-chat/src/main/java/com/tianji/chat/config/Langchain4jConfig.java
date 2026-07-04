package com.tianji.chat.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(prefix = "langchain4j", name = "enabled", matchIfMissing = true)
public class Langchain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(Langchain4jConfig.class);

    @Value("${langchain4j.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${langchain4j.api-key:}")
    private String apiKey;

    @Value("${langchain4j.max-tokens:2000}")
    private int maxTokens;

    @Value("${langchain4j.timeout-seconds:60}")
    private int timeoutSeconds;

    @Value("${langchain4j.model-name:glm-4.5-flash}")
    private String modelName;

    @Value("${langchain4j.embedding-model-name:embedding-3}")
    private String embeddingModelName;

    @Value("${langchain4j.max-retries:3}")
    private int maxRetries;

    @Value("${langchain4j.chat-model-temperature:0.7}")
    private double chatModelTemperature;

    @Value("${langchain4j.streaming-chat-model-temperature:0.7}")
    private double streamingChatModelTemperature;

    private void validateApiKey(String caller) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[{}] DASHSCOPE_API_KEY 未配置（langchain4j.api-key 为空）。" +
                    "请在操作系统环境变量 / IDE Run Configuration / 或项目根目录 .env 中设置 DASHSCOPE_API_KEY=sk-xxx。" +
                    "当前 Bean 已占位创建，但调用 DashScope API 会失败。", caller);
        }
    }

    @Bean
    public ChatLanguageModel qwenChatModel() {
        validateApiKey("qwenChatModel");
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .temperature(chatModelTemperature)
                .maxTokens(maxTokens)
                .tokenizer(new OpenAiTokenizer())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .maxRetries(maxRetries)
                .build();
    }


    @Bean
    public StreamingChatLanguageModel qwenStreamingChatModel() {
        validateApiKey("qwenStreamingChatModel");
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .temperature(streamingChatModelTemperature)
                .maxTokens(maxTokens)
                .tokenizer(new OpenAiTokenizer())
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .build();
    }


    @Bean
    public EmbeddingModel embeddingModel() {
        validateApiKey("embeddingModel");
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(embeddingModelName)
                .build();
    }
}