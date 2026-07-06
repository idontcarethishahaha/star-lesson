package com.tianji.aigc.application.chat.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.application.chat.dto.AgentChatDTO;
import com.tianji.aigc.application.rag.dto.RagSearchResultDTO;
import com.tianji.aigc.application.rag.service.RagSearchService;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.domain.agent.model.AgentEntity;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import com.tianji.aigc.domain.rag.model.FileDetailEntity;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.mapper.AgentMapper;
import com.tianji.aigc.mapper.AgentVersionMapper;
import com.tianji.aigc.mapper.FileDetailMapper;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentChatService {

    public static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();
    private static final String GENERATE_STATUS_KEY = "GENERATE_STATUS";
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6;
    private static final int DEFAULT_TOP_K = 6;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatSessionService chatSessionService;
    private final AgentMapper agentMapper;
    private final AgentVersionMapper agentVersionMapper;
    private final RagSearchService ragSearchService;
    private final FileDetailMapper fileDetailMapper;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;

    public Flux<ChatEventVO> chat(AgentChatDTO chatDTO) {
        String question = chatDTO.getQuestion();
        String sessionId = chatDTO.getSessionId();
        String agentId = chatDTO.getAgentId();

        var conversationId = getConversationId(sessionId);
        var outputBuilder = new StringBuilder();
        StringRedisTemplate stringRedisTemplate = this.stringRedisTemplateProvider.getIfAvailable();
        var hashOps = stringRedisTemplate != null ? stringRedisTemplate.boundHashOps(GENERATE_STATUS_KEY) : null;
        var requestId = IdUtil.simpleUUID();
        var userId = UserContext.getUser();

        AgentConfig agentConfig = loadAgentConfig(agentId);

        this.chatSessionService.update(sessionId, question, userId);

        String ragContext = buildRagContext(agentConfig, question);

        final String finalSystemPrompt;
        if (StrUtil.isNotBlank(ragContext)) {
            finalSystemPrompt = agentConfig.systemPrompt() + "\n\n" + ragContext;
        } else {
            finalSystemPrompt = agentConfig.systemPrompt();
        }

        ChatClient.ChatClientRequestSpec prompt = this.chatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(finalSystemPrompt)
                        .params(Map.of("now", DateUtil.now()))
                )
                .advisors(advisor -> {
                    advisor.param(ChatMemory.CONVERSATION_ID, conversationId);
                })
                .toolContext(Map.of(Constant.REQUEST_ID, requestId, Constant.USER_ID, userId))
                .user(question);

        return prompt.stream()
                .chatResponse()
                .doFirst(() -> {
                    if (hashOps != null) hashOps.put(sessionId, "true");
                })
                .doOnError(throwable -> {
                    if (hashOps != null) hashOps.delete(sessionId);
                })
                .doOnComplete(() -> {
                    if (hashOps != null) hashOps.delete(sessionId);
                })
                .doOnCancel(() -> {
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .takeWhile(response -> hashOps == null || hashOps.get(sessionId) != null)
                .map(chatResponse -> {
                    var text = chatResponse.getResult().getOutput().getText();
                    outputBuilder.append(text);

                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(finishReason, Constant.STOP)) {
                        var messageId = chatResponse.getMetadata().getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }

                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    var result = ToolResultHolder.get(requestId);
                    if (ObjectUtil.isNotEmpty(result)) {
                        ToolResultHolder.remove(requestId);
                        return Flux.just(ChatEventVO.builder()
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .eventData(result)
                                .build(), STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    public void stop(String sessionId) {
        StringRedisTemplate stringRedisTemplate = this.stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate != null) {
            var hashOps = stringRedisTemplate.boundHashOps(GENERATE_STATUS_KEY);
            hashOps.delete(sessionId);
        }
    }

    public String chatText(String question, String agentId) {
        AgentConfig agentConfig = loadAgentConfig(agentId);
        String ragContext = buildRagContext(agentConfig, question);
        String systemPrompt = agentConfig.systemPrompt();
        if (StrUtil.isNotBlank(ragContext)) {
            systemPrompt = systemPrompt + "\n\n" + ragContext;
        }
        return this.chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call().content();
    }

    public String getWelcomeMessage(String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            return "你好！我是天骄星课堂的AI助手 (≧∇≦)ﾉ 有什么可以帮你的吗？";
        }
        AgentEntity agent = agentMapper.selectById(agentId);
        if (agent == null || Boolean.FALSE.equals(agent.getEnabled())) {
            return "你好！我是天骄星课堂的AI助手 (≧∇≦)ﾉ 有什么可以帮你的吗？";
        }
        String welcomeMessage = agent.getWelcomeMessage();
        if (welcomeMessage == null || welcomeMessage.isEmpty()) {
            welcomeMessage = "你好！我是 " + agent.getName() + " (≧∇≦)ﾉ 有什么可以帮你的吗？";
        }
        return welcomeMessage;
    }

    private String buildRagContext(AgentConfig agentConfig, String query) {
        List<String> knowledgeBaseIds = agentConfig.knowledgeBaseIds();
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return "";
        }

        try {
            List<RagSearchResultDTO> results = ragSearchService.search(
                    knowledgeBaseIds,
                    query,
                    DEFAULT_SIMILARITY_THRESHOLD,
                    DEFAULT_TOP_K
            );

            if (results == null || results.isEmpty()) {
                return "";
            }

            StringBuilder context = new StringBuilder();
            context.append("以下是从知识库中检索到的相关参考资料，请基于这些资料回答用户问题，如果资料中没有相关内容，请根据你的知识回答：\n\n");

            int index = 1;
            for (RagSearchResultDTO result : results) {
                context.append("【资料").append(index).append("】");
                if (result.getFileId() != null) {
                    FileDetailEntity file = fileDetailMapper.selectById(result.getFileId());
                    if (file != null) {
                        context.append("（文件：").append(file.getOriginalFilename()).append("）");
                    }
                }
                context.append("\n");
                context.append(result.getContent()).append("\n\n");
                index++;
            }

            return context.toString();
        } catch (Exception e) {
            log.error("RAG 检索失败, query={}", query, e);
            return "";
        }
    }

    private AgentConfig loadAgentConfig(String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            return AgentConfig.defaultConfig();
        }

        AgentEntity agent = agentMapper.selectById(agentId);
        if (agent == null) {
            log.warn("Agent不存在: {}, 使用默认配置", agentId);
            return AgentConfig.defaultConfig();
        }

        if (Boolean.FALSE.equals(agent.getEnabled())) {
            throw new BusinessException("Agent未启用");
        }

        String systemPrompt = agent.getSystemPrompt();
        List<String> knowledgeBaseIds = agent.getKnowledgeBaseIds();

        if (agent.getPublishedVersion() != null) {
            AgentVersionEntity version = agentVersionMapper.selectById(agent.getPublishedVersion());
            if (version != null) {
                if (version.getSystemPrompt() != null) {
                    systemPrompt = version.getSystemPrompt();
                }
                if (version.getKnowledgeBaseIds() != null) {
                    knowledgeBaseIds = version.getKnowledgeBaseIds();
                }
            }
        }

        return new AgentConfig(systemPrompt, knowledgeBaseIds);
    }

    private void saveStopHistoryRecord(String conversationId, String content) {
        this.chatMemory.add(conversationId, new AssistantMessage(content));
    }

    private static String getConversationId(String sessionId) {
        return UserContext.getUser() + "_" + sessionId;
    }

    private record AgentConfig(String systemPrompt, List<String> knowledgeBaseIds) {

        static AgentConfig defaultConfig() {
            return new AgentConfig(
                    "你是天骄星课堂的AI学习助手，名叫「学堂小助手」(≧∇≦)ﾉ\n" +
                            "你的使命是帮助学生更好地学习和成长。\n\n" +
                            "【你可以做的事情】\n" +
                            "1. 📚 课程咨询：查询课程信息、课程对比、课程推荐\n" +
                            "2. 📝 学习笔记：为课程生成学习笔记、思维导图\n" +
                            "3. 📅 学习计划：根据课程内容和时间制定学习计划\n" +
                            "4. 📊 学习报告：查看学习进度、生成学习报告\n" +
                            "5. ✅ 学习打卡：记录学习、获取激励、查看打卡日历\n" +
                            "6. 💪 加油鼓励：学习累了、不想学了，可以找我打气\n" +
                            "7. 🎯 学习建议：根据学习情况给出个性化建议\n\n" +
                            "【你的性格】\n" +
                            "- 活泼开朗，喜欢用猫咪表情 (｡•ᴗ-｡)♡ (=^･ω･^=)\n" +
                            "- 耐心友好，善于鼓励学生\n" +
                            "- 专业可靠，回答准确有深度\n" +
                            "- 会主动询问学生的需求，提供帮助\n\n" +
                            "【注意事项】\n" +
                            "- 优先使用工具获取真实的课程和学习数据\n" +
                            "- 如果用户问的问题需要课程信息，先调用工具查询\n" +
                            "- 用亲切的语气回答，让学生感到温暖\n" +
                            "- 鼓励为主，即使学生进度慢也要给予肯定",
                    List.of()
            );
        }

        boolean hasKnowledgeBase() {
            return knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty();
        }
    }
}
