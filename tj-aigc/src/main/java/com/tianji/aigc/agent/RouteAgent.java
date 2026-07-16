package com.tianji.aigc.agent;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final AgentContextBus agentContextBus;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.ROUTE;
    }

    @Override
    public String systemMessage() {
        String conversationHistory = agentContextBus.getConversationHistory(this.getCurrentSessionId());
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是智能路由助手，负责根据用户问题的意图，将请求分配给最合适的子智能体处理。\n\n");
        prompt.append("可用的子智能体：\n");
        prompt.append("- RECOMMEND（课程推荐）：根据用户兴趣、学习需求推荐合适的课程\n");
        prompt.append("- CONSULT（课程咨询）：回答用户关于课程内容、价格、时长、讲师、大纲等具体问题\n");
        prompt.append("- BUY（课程购买）：帮助用户下单、结算、确认订单等购买相关操作\n\n");
        prompt.append("注意事项：\n");
        prompt.append("1. 如果用户的问题涉及多个领域（如'推荐课程并帮我购买'），请按顺序列出需要调用的智能体\n");
        prompt.append("2. 如果用户之前已经咨询过某个课程，请优先考虑购买意图\n");
        prompt.append("3. 如果用户之前已经获得过课程推荐，请根据推荐结果判断下一步意图\n\n");
        
        if (StrUtil.isNotBlank(conversationHistory)) {
            prompt.append("当前会话历史：\n").append(conversationHistory).append("\n\n");
        }
        
        prompt.append("输出格式要求：\n");
        prompt.append("请输出一个JSON对象，不要输出任何其他内容，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"agentName\": \"目标智能体名称\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"reason\": \"简要判断理由\",\n");
        prompt.append("  \"nextAgent\": \"后续需要调用的智能体（可选，如果有）\"\n");
        prompt.append("}\n\n");
        prompt.append("agentName 只能是 RECOMMEND、CONSULT、BUY 三者之一。\n");
        prompt.append("confidence 是 0 到 1 之间的浮点数，表示你对判断的把握程度。\n");
        prompt.append("nextAgent 用于多智能体协作场景，表示当前智能体处理完后需要调用的下一个智能体。\n");
        
        return prompt.toString();
    }

    private String currentSessionId;

    public void setCurrentSessionId(String sessionId) {
        this.currentSessionId = sessionId;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    @Override
    public String process(String question, String sessionId) {
        this.setCurrentSessionId(sessionId);
        agentContextBus.appendConversationHistory(sessionId, "[路由] " + question);
        return super.process(question, sessionId);
    }

    @Override
    public reactor.core.publisher.Flux<com.tianji.aigc.vo.ChatEventVO> processStream(String question, String sessionId) {
        this.setCurrentSessionId(sessionId);
        agentContextBus.appendConversationHistory(sessionId, "[路由] " + question);
        return super.processStream(question, sessionId);
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = UserContext.getUser();
        return Map.of(
                Constant.REQUEST_ID, requestId,
                Constant.USER_ID, userId);
    }
}
