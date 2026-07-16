package com.tianji.aigc.agent;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.application.rag.dto.RagSearchResultDTO;
import com.tianji.aigc.application.rag.service.RagSearchService;
import com.tianji.aigc.config.AIProperties;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.CourseTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final AIProperties aiProperties;
    private final RagSearchService ragSearchService;
    private final CourseTools courseTools;
    private final AgentContextBus agentContextBus;

    @Lazy
    private final BuyAgent buyAgent;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.RECOMMEND;
    }

    @Override
    public String systemMessage() {
        return systemPromptConfig.getRecommendAgentSystemMessage().get();
    }

    @Override
    public Object[] tools() {
        return new Object[]{this.courseTools};
    }

    @Override
    public String buildRagContext(String question) {
        List<String> knowledgeBaseIds = aiProperties.getRag() != null
                ? aiProperties.getRag().getRecommendKnowledgeBaseIds() : null;
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
        String result = super.process(question, sessionId);
        agentContextBus.setRecommendedCourses(sessionId, result);
        agentContextBus.appendConversationHistory(sessionId, "[推荐] " + question + " → " + truncateResult(result));
        
        if (isPurchaseFollowUp(question)) {
            log.info("RecommendAgent detected purchase follow-up intent, routing to BuyAgent");
            return buyAgent.process("基于以下推荐课程帮我下单：\n" + result, sessionId);
        }
        
        return result;
    }

    @Override
    public reactor.core.publisher.Flux<com.tianji.aigc.vo.ChatEventVO> processStream(String question, String sessionId) {
        agentContextBus.setCurrentTask(sessionId, "推荐课程");
        
        return super.processStream(question, sessionId)
                .doOnComplete(() -> {
                    String result = agentContextBus.getRecommendedCourses(sessionId);
                    if (StrUtil.isNotBlank(result) && isPurchaseFollowUp(question)) {
                        log.info("RecommendAgent stream detected purchase follow-up, routing to BuyAgent");
                    }
                });
    }

    private boolean isPurchaseFollowUp(String question) {
        String lower = question.toLowerCase();
        return lower.contains("买") || lower.contains("下单") || lower.contains("购买") 
                || lower.contains("然后") || lower.contains("之后") || lower.contains("帮我");
    }

    private String truncateResult(String result) {
        if (result.length() > 100) {
            return result.substring(0, 100) + "...";
        }
        return result;
    }
}
