package com.tianji.aigc.application.chat.service;

import com.tianji.aigc.advisor.RecordOptimizationAdvisor;
import com.tianji.aigc.memory.MyChatMemoryRepository;
import com.tianji.aigc.tools.CheckInTools;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.LearningReportTools;
import com.tianji.aigc.tools.LearningTools;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.aigc.tools.StudyPlanTools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentChatClientFactory {

    @Qualifier("dashscopeChatModel")
    private final ChatModel dashscopeChatModel;
    private final ChatMemory chatMemory;
    private final CourseTools courseTools;
    private final OrderTools orderTools;
    private final LearningTools learningTools;
    private final StudyPlanTools studyPlanTools;
    private final LearningReportTools learningReportTools;
    private final CheckInTools checkInTools;
    private final MyChatMemoryRepository myChatMemoryRepository;

    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    private Advisor loggerAdvisor;
    private Advisor messageChatMemoryAdvisor;
    private Advisor recordOptimizationAdvisor;

    private Map<String, Object> toolBeanMap;

    @PostConstruct
    public void init() {
        this.loggerAdvisor = new SimpleLoggerAdvisor();
        this.messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.recordOptimizationAdvisor = new RecordOptimizationAdvisor(myChatMemoryRepository);

        this.toolBeanMap = new HashMap<>();
        toolBeanMap.put("courseTools", courseTools);
        toolBeanMap.put("orderTools", orderTools);
        toolBeanMap.put("learningTools", learningTools);
        toolBeanMap.put("studyPlanTools", studyPlanTools);
        toolBeanMap.put("learningReportTools", learningReportTools);
        toolBeanMap.put("checkInTools", checkInTools);

        log.info("[AgentChatClientFactory] 初始化完成，可用工具: {}", toolBeanMap.keySet());
    }

    public ChatClient getChatClient(List<String> toolBeanNames) {
        if (toolBeanNames == null || toolBeanNames.isEmpty()) {
            return buildChatClient(new Object[0]);
        }
        String cacheKey = String.join(",", toolBeanNames);
        return chatClientCache.computeIfAbsent(cacheKey, k -> {
            Object[] tools = toolBeanNames.stream()
                    .map(toolBeanMap::get)
                    .filter(bean -> bean != null)
                    .toArray();
            log.info("[AgentChatClientFactory] 创建新的 ChatClient，工具: {}", toolBeanNames);
            return buildChatClient(tools);
        });
    }

    public ChatClient getChatClientAllTools() {
        return chatClientCache.computeIfAbsent("__all__", k -> {
            Object[] tools = toolBeanMap.values().toArray();
            log.info("[AgentChatClientFactory] 创建全工具 ChatClient");
            return buildChatClient(tools);
        });
    }

    private ChatClient buildChatClient(Object[] tools) {
        return ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor, recordOptimizationAdvisor)
                .defaultTools(tools)
                .build();
    }

    public Object resolveToolBean(String beanName) {
        return toolBeanMap.get(beanName);
    }
}
