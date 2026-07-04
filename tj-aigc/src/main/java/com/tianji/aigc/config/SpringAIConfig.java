package com.tianji.aigc.config;

import com.tianji.aigc.advisor.RecordOptimizationAdvisor;
import com.tianji.aigc.memory.MyChatMemoryRepository;
import com.tianji.aigc.memory.RedisChatMemoryRepository;
import com.tianji.aigc.memory.SummarizingChatMemory;
import com.tianji.aigc.memory.TokenCounter;
import com.tianji.aigc.memory.jdbc.JdbcChatMemoryRepository;
import com.tianji.aigc.service.ChatRecordService;
import com.tianji.aigc.tools.CheckInTools;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.LearningReportTools;
import com.tianji.aigc.tools.LearningTools;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.aigc.tools.StudyPlanTools;
import com.tianji.common.constants.Constant;
import com.tianji.common.utils.WebUtils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SpringAIConfig {

    @Value("${tj.ai.memory.max:100}")
    private Integer maxMessages;

    @Value("${tj.ai.memory.strategy:message_window}")
    private String memoryStrategy;

    @Value("${tj.ai.memory.maxTokens:8000}")
    private Integer maxTokenLimit;

    @Value("${tj.ai.memory.summaryThreshold:4000}")
    private Integer summaryThreshold;

    /**
     * 创建并配置自定义重试监听器Bean
     * <p>
     * 实现说明：
     * 1. 创建匿名RetryListener实现，在重试操作期间管理Web属性
     * 2. 将监听器注册到提供的RetryTemplate实例
     *
     * @param retryTemplate Spring Retry模板对象，用于注册重试监听器
     * @return RetryListener 已注册到模板的重试监听器实例，将由Spring容器管理
     */
    @Bean
    public RetryListener customizeRetryTemplate(RetryTemplate retryTemplate) {
        // 创建自定义重试监听器，实现以下核心功能：
        // - 重试开始时设置上下文标识
        // - 重试结束后清理上下文标识
        RetryListener retryListener = new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                WebUtils.setAttribute(Constant.SPRING_AI_ATTR, Constant.SPRING_AI_FLAG);
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                WebUtils.removeAttribute(Constant.SPRING_AI_ATTR);
            }
        };

        // 将监听器注册到重试模板
        retryTemplate.registerListener(retryListener);
        return retryListener;
    }

    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient chatClient(@Qualifier("dashscopeChatModel") ChatModel dashScopeChatModel,
                                 Advisor loggerAdvisor,
                                 Advisor messageChatMemoryAdvisor,
                                 Advisor recordOptimizationAdvisor,
                                 CourseTools courseTools,
                                 OrderTools orderTools,
                                 LearningTools learningTools,
                                 StudyPlanTools studyPlanTools,
                                 LearningReportTools learningReportTools,
                                 CheckInTools checkInTools
    ) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor, recordOptimizationAdvisor)
                .defaultTools(courseTools, orderTools, learningTools, studyPlanTools, learningReportTools, checkInTools)
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                       Advisor loggerAdvisor  // 日志记录器
    ) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor)
                .build();
    }

    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tj.ai.memory", value = "type", havingValue = "Redis")
    public ChatMemoryRepository redisChatMemoryRepository(ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        RedisChatMemoryRepository repository = new RedisChatMemoryRepository();
        StringRedisTemplate template = stringRedisTemplateProvider.getIfAvailable();
        if (template != null) {
            repository.setStringRedisTemplate(template);
        }
        return repository;
    }

    @Bean
    @ConditionalOnProperty(prefix = "tj.ai.memory", value = "type", havingValue = "MYSQL")
    public ChatMemoryRepository jdbcChatMemoryRepository(ObjectProvider<ChatRecordService> chatRecordServiceProvider) {
        JdbcChatMemoryRepository repository = new JdbcChatMemoryRepository();
        ChatRecordService service = chatRecordServiceProvider.getIfAvailable();
        if (service != null) {
            repository.setChatRecordService(service);
        }
        return repository;
    }

    // MongoDB ChatMemoryRepository 已在 tj-chat 迁移时废弃，不再需要
    // @Bean
    // @ConditionalOnProperty(prefix = "tj.ai.memory", value = "type", havingValue = "MongoDB")
    // public ChatMemoryRepository mongoDBChatMemoryRepository(...) { ... }

    @Bean
    public TokenCounter tokenCounter() {
        return new TokenCounter();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository,
                                 @Qualifier("dashscopeChatModel") ChatModel chatModel,
                                 TokenCounter tokenCounter) {
        if ("summarizing".equalsIgnoreCase(memoryStrategy)) {
            return new SummarizingChatMemory(
                    chatMemoryRepository,
                    chatModel,
                    tokenCounter,
                    maxTokenLimit,
                    summaryThreshold
            );
        }
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(this.maxMessages)
                .build();
    }

    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public Advisor recordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository){
        return new RecordOptimizationAdvisor(myChatMemoryRepository);
    }

    /**
     * 创建 VectorStore Bean
     * 将 LangChain4j EmbeddingStore 适配到 Spring AI VectorStore
     */
    @Bean
    public VectorStore vectorStore(EmbeddingStore<TextSegment> embeddingStore,
                                   EmbeddingModel embeddingModel) {
        return new LangChain4jEmbeddingStoreAdapter(embeddingStore, embeddingModel);
    }
}
