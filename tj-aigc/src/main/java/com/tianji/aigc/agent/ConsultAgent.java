package com.tianji.aigc.agent;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.application.rag.dto.RagSearchResultDTO;
import com.tianji.aigc.application.rag.service.RagSearchService;
import com.tianji.aigc.config.AIProperties;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.OrderTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final AIProperties aiProperties;
    private final RagSearchService ragSearchService;
    private final CourseTools courseTools;
    private final AgentContextBus agentContextBus;

    @Lazy
    private final BuyAgent buyAgent;

    @Lazy
    private final RecommendAgent recommendAgent;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.CONSULT;
    }

    @Override
    public String systemMessage() {
        return systemPromptConfig.getConsultAgentSystemMessage().get();
    }

    @Override
    public Object[] tools() {
        return new Object[]{this.courseTools};
    }

    @Override
    public String buildRagContext(String question) {
        List<String> knowledgeBaseIds = aiProperties.getRag() != null
                ? aiProperties.getRag().getConsultKnowledgeBaseIds() : null;
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return "";
        }
        try {
            double threshold = aiProperties.getRag().getSimilarityThreshold();
            int topK = aiProperties.getRag().getTopK();
            List<RagSearchResultDTO> results = ragSearchService.search(
                    knowledgeBaseIds, question, threshold, topK);
            if (results == null || results.isEmpty()) {
                return "";
            }
            StringBuilder context = new StringBuilder();
            context.append("以下是从知识库中检索到的相关参考资料，请基于这些资料回答用户问题，如果资料中没有相关内容，请根据你的知识回答：\n\n");
            int index = 1;
            for (RagSearchResultDTO result : results) {
                context.append("【资料").append(index).append("】\n");
                context.append(result.getContent()).append("\n\n");
                index++;
            }
            return context.toString();
        } catch (Exception e) {
            log.warn("RAG 检索失败, question={}", question, e);
            return "";
        }
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of(Constant.REQUEST_ID, requestId);
    }

    @Override
    public String process(String question, String sessionId) {
        if (isPurchaseRelated(question)) {
            log.info("ConsultAgent detected purchase intent, routing to BuyAgent");
            String courseName = extractCourseName(question);
            if (StrUtil.isNotBlank(courseName)) {
                agentContextBus.setConsultedCourse(sessionId, courseName);
            }
            agentContextBus.appendConversationHistory(sessionId, "[咨询] " + question);
            return buyAgent.process(question, sessionId);
        }
        
        if (isRecommendationRelated(question)) {
            log.info("ConsultAgent detected recommendation intent, routing to RecommendAgent");
            agentContextBus.appendConversationHistory(sessionId, "[咨询] " + question);
            return recommendAgent.process(question, sessionId);
        }
        
        return super.process(question, sessionId);
    }

    @Override
    public reactor.core.publisher.Flux<com.tianji.aigc.vo.ChatEventVO> processStream(String question, String sessionId) {
        if (isPurchaseRelated(question)) {
            log.info("ConsultAgent stream detected purchase intent, routing to BuyAgent");
            String courseName = extractCourseName(question);
            if (StrUtil.isNotBlank(courseName)) {
                agentContextBus.setConsultedCourse(sessionId, courseName);
            }
            agentContextBus.appendConversationHistory(sessionId, "[咨询] " + question);
            return buyAgent.processStream(question, sessionId);
        }
        
        if (isRecommendationRelated(question)) {
            log.info("ConsultAgent stream detected recommendation intent, routing to RecommendAgent");
            agentContextBus.appendConversationHistory(sessionId, "[咨询] " + question);
            return recommendAgent.processStream(question, sessionId);
        }
        
        return super.processStream(question, sessionId);
    }

    private boolean isPurchaseRelated(String question) {
        String lower = question.toLowerCase();
        return lower.contains("买") || lower.contains("下单") || lower.contains("购买") 
                || lower.contains("支付") || lower.contains("价格") || lower.contains("多少钱");
    }

    private boolean isRecommendationRelated(String question) {
        String lower = question.toLowerCase();
        return lower.contains("推荐") || lower.contains("选") || lower.contains("适合");
    }

    private String extractCourseName(String question) {
        int start = question.indexOf("《");
        int end = question.indexOf("》");
        if (start != -1 && end != -1 && end > start) {
            return question.substring(start + 1, end);
        }
        return null;
    }
}
