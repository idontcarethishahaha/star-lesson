package com.tianji.aigc.infrastructure.initializer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.aigc.domain.agent.constant.PublishStatus;
import com.tianji.aigc.domain.agent.model.AgentEntity;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import com.tianji.aigc.mapper.AgentMapper;
import com.tianji.aigc.mapper.AgentVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresetAgentInitializer implements ApplicationRunner {

    private final AgentMapper agentMapper;
    private final AgentVersionMapper agentVersionMapper;
    private final DataSource dataSource;

    public static final String ROUTE_AGENT_ID = "preset_route";
    public static final String RECOMMEND_AGENT_ID = "preset_recommend";
    public static final String CONSULT_AGENT_ID = "preset_consult";
    public static final String BUY_AGENT_ID = "preset_buy";

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureTableExists();
            initPresetAgents();
        } catch (Exception e) {
            log.warn("预置Agent初始化失败，可能是表结构不完整: {}", e.getMessage());
        }
    }

    private void ensureTableExists() {
        try (Connection conn = dataSource.getConnection()) {
            String[] sqls = {
                "ALTER TABLE agents ADD COLUMN IF NOT EXISTS tool_ids TEXT NULL",
                "ALTER TABLE agents ADD COLUMN IF NOT EXISTS knowledge_base_ids TEXT NULL",
                "ALTER TABLE agents ADD COLUMN IF NOT EXISTS tool_preset_params TEXT NULL",
                "ALTER TABLE agents ADD COLUMN IF NOT EXISTS multi_modal TINYINT(1) DEFAULT 0",
                "ALTER TABLE agents ADD COLUMN IF NOT EXISTS llm_model_config VARCHAR(255) NULL"
            };
            for (String sql : sqls) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.execute();
                } catch (Exception e) {
                    log.debug("列可能已存在: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("检查agents表结构失败", e);
        }
    }

    private void initPresetAgents() {
        initAgent(ROUTE_AGENT_ID, "路由智能体",
                "负责将用户问题分配给合适的子智能体进行处理",
                buildRouteSystemPrompt(),
                List.of(), List.of(),
                "你好！我是智能路由助手 (≧∇≦)ﾉ 有什么问题尽管问我，我会帮你找到最合适的解答！");

        initAgent(RECOMMEND_AGENT_ID, "课程推荐智能体",
                "根据用户兴趣、学习需求推荐合适的课程",
                "你是课程推荐智能体，负责根据用户兴趣推荐合适的课程。请结合用户的学习背景、兴趣爱好和职业规划，为用户推荐最适合的课程。",
                List.of("courseTools"), List.of(),
                "你好！我是课程推荐助手 (≧∇≦)ﾉ 告诉我你的学习需求，我来帮你找到最合适的课程！");

        initAgent(CONSULT_AGENT_ID, "课程咨询智能体",
                "回答用户关于课程内容、价格、时长、讲师、大纲等具体问题",
                "你是课程咨询助手，负责回答用户关于课程内容、价格、时长、讲师、大纲等问题。请用友好、专业的语气解答用户的疑问。",
                List.of("courseTools"), List.of(),
                "你好！我是课程咨询助手 (≧∇≦)ﾉ 有任何关于课程的问题都可以问我哦！");

        initAgent(BUY_AGENT_ID, "课程购买智能体",
                "帮助用户下单、结算、确认订单等购买相关操作",
                "你是购课助手，负责帮助用户了解和购买课程。请引导用户完成选课、确认订单和支付流程。",
                List.of("orderTools", "courseTools"), List.of(),
                "你好！我是购课助手 (≧∇≦)ﾉ 需要帮你下单购买课程吗？");

        log.info("预置Agent初始化完成");
    }

    private void initAgent(String id, String name, String description,
                           String systemPrompt, List<String> toolIds,
                           List<String> knowledgeBaseIds, String welcomeMessage) {
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getId, id);
        AgentEntity existing = agentMapper.selectOne(wrapper);

        if (existing == null) {
            AgentEntity agent = new AgentEntity();
            agent.setId(id);
            agent.setName(name);
            agent.setDescription(description);
            agent.setSystemPrompt(systemPrompt);
            agent.setToolIds(toolIds);
            agent.setKnowledgeBaseIds(knowledgeBaseIds);
            agent.setWelcomeMessage(welcomeMessage);
            agent.setEnabled(true);
            agent.setUserId("system");
            agent.setCreatedAt(java.time.LocalDateTime.now());
            agent.setUpdatedAt(java.time.LocalDateTime.now());
            agentMapper.insert(agent);

            String versionId = id + "_v1";
            LambdaQueryWrapper<AgentVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
            versionWrapper.eq(AgentVersionEntity::getId, versionId);
            AgentVersionEntity existingVersion = agentVersionMapper.selectOne(versionWrapper);

            if (existingVersion == null) {
                AgentVersionEntity version = new AgentVersionEntity();
                version.setId(versionId);
                version.setAgentId(id);
                version.setName(name);
                version.setDescription(description);
                version.setVersionNumber("1.0.0");
                version.setSystemPrompt(systemPrompt);
                version.setWelcomeMessage(welcomeMessage);
                version.setToolIds(toolIds);
                version.setKnowledgeBaseIds(knowledgeBaseIds);
                version.setChangeLog("初始版本");
                version.setPublishStatus(PublishStatus.PUBLISHED.getCode());
                version.setUserId("system");
                version.setCreatedAt(java.time.LocalDateTime.now());
                version.setUpdatedAt(java.time.LocalDateTime.now());
                version.setPublishedAt(java.time.LocalDateTime.now());
                version.setReviewTime(java.time.LocalDateTime.now());
                agentVersionMapper.insert(version);
            }

            agent.setPublishedVersion(versionId);
            agentMapper.updateById(agent);

            log.info("预置Agent创建成功: {} - {}，版本: 1.0.0", id, name);
        } else {
            log.debug("预置Agent已存在: {} - {}", id, name);
        }
    }

    private String buildRouteSystemPrompt() {
        return """
            你是路由智能体，负责将用户问题分配给合适的子智能体。

            可选的子智能体：
            - RECOMMEND：课程推荐智能体，负责根据用户兴趣、需求推荐合适的课程
            - CONSULT：课程咨询智能体，负责回答用户关于课程内容、价格、时长、讲师、大纲等具体问题
            - BUY：课程购买智能体，负责帮助用户下单、结算、确认订单等购买相关操作

            请根据用户问题，判断应该路由到哪个智能体。

            输出要求：只输出一个 JSON 对象，不要输出任何其他内容，格式如下：
            {
              "agentName": "智能体名称",
              "confidence": 0.95,
              "reason": "简要判断理由"
            }

            agentName 只能是 RECOMMEND、CONSULT、BUY 三者之一。
            confidence 是 0 到 1 之间的浮点数，表示你对判断的把握程度。
            """;
    }
}
