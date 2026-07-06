package com.tianji.aigc.agent;

import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.CourseTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 课程咨询智能体
 * 结合 RAG 知识库检索 + CourseTools 工具调用
 */
@Component
@RequiredArgsConstructor
public class ConsultAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final CourseTools courseTools;

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.CONSULT;
    }

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getConsultAgentSystemMessage().get();
    }

    @Override
    public Object[] tools() {
        return new Object[]{this.courseTools};
    }

    @Override
    public List<Advisor> advisors() {
        // RAG 增强：从知识库检索相关文档片段作为上下文
        var qaAdvisor = QuestionAnswerAdvisor.builder(this.vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.6d) // 相似度阈值
                        .topK(6) // 搜索的条数
                        .build())
                .build();

        return List.of(qaAdvisor);
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of(Constant.REQUEST_ID, requestId);
    }
}