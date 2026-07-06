package com.tianji.aigc.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final NacosConfigManager nacosConfigManager;
    private final AIProperties aiProperties;

    // 使用原子引用，保证线程安全
    private final AtomicReference<String> chatSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> routeAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> recommendAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> consultAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> buyAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> textSystemMessage = new AtomicReference<>();

    @PostConstruct // 初始化时加载配置
    public void init() {
        // 先设置默认值
        chatSystemMessage.set(DEFAULT_CHAT_SYSTEM_MESSAGE);
        routeAgentSystemMessage.set(DEFAULT_ROUTE_AGENT_MESSAGE);
        recommendAgentSystemMessage.set(DEFAULT_RECOMMEND_AGENT_MESSAGE);
        consultAgentSystemMessage.set(DEFAULT_CONSULT_AGENT_MESSAGE);
        buyAgentSystemMessage.set(DEFAULT_BUY_AGENT_MESSAGE);
        textSystemMessage.set(DEFAULT_TEXT_MESSAGE);
        // 尝试从 Nacos 读取配置
        try {
            loadConfig(aiProperties.getSystem().getChat(), chatSystemMessage);
            loadConfig(aiProperties.getSystem().getRouteAgent(), routeAgentSystemMessage);
            loadConfig(aiProperties.getSystem().getRecommendAgent(), recommendAgentSystemMessage);
            loadConfig(aiProperties.getSystem().getConsultAgent(), consultAgentSystemMessage);
            loadConfig(aiProperties.getSystem().getBuyAgent(), buyAgentSystemMessage);
            loadConfig(aiProperties.getSystem().getText(), textSystemMessage);
        } catch (Exception e) {
            log.warn("无法从Nacos加载配置，使用默认系统提示词: {}", e.getMessage());
        }
    }

    private static final String DEFAULT_CHAT_SYSTEM_MESSAGE = """
            你是天骄星课堂的AI学习助手，叫"小天"。你需要：
            1. 用友好、亲切的语气回答用户关于课程学习的问题
            2. 如果用户询问课程信息，可以使用课程查询工具
            3. 如果用户询问学习进度，可以使用学习进度工具
            4. 不知道的问题直接告诉用户，不要编造
            5. 当前时间：{now}
            """;

    private static final String DEFAULT_ROUTE_AGENT_MESSAGE = "你是路由智能体，负责将用户问题分配给合适的子智能体。";
    private static final String DEFAULT_RECOMMEND_AGENT_MESSAGE = "你是课程推荐智能体，负责根据用户兴趣推荐合适的课程。";
    private static final String DEFAULT_CONSULT_AGENT_MESSAGE = "你是课程咨询助手，负责回答用户关于课程内容、价格、时长等问题。";
    private static final String DEFAULT_BUY_AGENT_MESSAGE = "你是购课助手，负责帮助用户了解和购买课程。";
    private static final String DEFAULT_TEXT_MESSAGE = "你是文本处理助手，负责问答回复、润色等文本类业务。";

    private void loadConfig(AIProperties.System.Chat chatConfig, AtomicReference<String> target) {
        try {
            var dataId = chatConfig.getDataId();
            var group = chatConfig.getGroup();
            var timeoutMs = chatConfig.getTimeoutMs();

            // 读取配置文件中的内容
            var config = nacosConfigManager.getConfigService().getConfig(dataId, group, timeoutMs);
            target.set(config);
            log.info("读取{}成功，内容为：{}", target, config);

            // 设置监听事件，用于热更新
            nacosConfigManager.getConfigService().addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String info) {
                    target.set(info);
                    log.info("更新{}成功，内容为：{}", target, info);
                }
            });
        } catch (Exception e) {
            log.error("加载配置失败", e);
        }
    }

}
