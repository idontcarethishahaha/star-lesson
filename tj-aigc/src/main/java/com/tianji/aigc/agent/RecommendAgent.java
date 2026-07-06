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

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.RECOMMEND;
    }

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRecommendAgentSystemMessage().get();
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
}
