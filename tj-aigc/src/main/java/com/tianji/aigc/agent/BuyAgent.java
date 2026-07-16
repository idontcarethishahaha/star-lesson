package com.tianji.aigc.agent;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final OrderTools orderTools;

    @Lazy
    private final RecommendAgent recommendAgent;

    private final AgentContextBus agentContextBus;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.BUY;
    }

    @Override
    public String systemMessage() {
        String recommendedCourses = agentContextBus.getRecommendedCourses(this.getCurrentSessionId());
        if (StrUtil.isNotBlank(recommendedCourses)) {
            return "以下是已推荐的课程信息，请参考：\n" + recommendedCourses + "\n\n"
                    + systemPromptConfig.getBuyAgentSystemMessage().get();
        }
        return systemPromptConfig.getBuyAgentSystemMessage().get();
    }

    @Override
    public Object[] tools() {
        return new Object[]{this.orderTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = UserContext.getUser();
        return Map.of(
                Constant.USER_ID, userId,
                Constant.REQUEST_ID, requestId);
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
        
        String recommendedCourses = agentContextBus.getRecommendedCourses(sessionId);
        if (StrUtil.isBlank(recommendedCourses) && isRecommendationNeeded(question)) {
            log.info("BuyAgent detected recommendation need, calling RecommendAgent");
            recommendedCourses = recommendAgent.process(question, sessionId);
            agentContextBus.setRecommendedCourses(sessionId, recommendedCourses);
            question = "基于以下推荐课程帮我下单：\n" + recommendedCourses + "\n\n用户原始需求：" + question;
        }
        
        return super.process(question, sessionId);
    }

    @Override
    public reactor.core.publisher.Flux<com.tianji.aigc.vo.ChatEventVO> processStream(String question, String sessionId) {
        this.setCurrentSessionId(sessionId);
        
        String recommendedCourses = agentContextBus.getRecommendedCourses(sessionId);
        if (StrUtil.isBlank(recommendedCourses) && isRecommendationNeeded(question)) {
            log.info("BuyAgent detected recommendation need in stream, calling RecommendAgent");
            recommendedCourses = recommendAgent.process(question, sessionId);
            agentContextBus.setRecommendedCourses(sessionId, recommendedCourses);
        }
        
        return super.processStream(question, sessionId);
    }

    private boolean isRecommendationNeeded(String question) {
        String lower = question.toLowerCase();
        return lower.contains("推荐") || lower.contains("选") || lower.contains("适合") 
                || lower.contains("哪个") || lower.contains("什么")
                || (lower.contains("买") && lower.contains("课程") && !lower.contains("下单"));
    }
}
