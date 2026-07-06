package com.tianji.aigc.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.agent.AbstractAgent;
import com.tianji.aigc.agent.Agent;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "chat-type", havingValue = "ROUTE")
public class AgentServiceImpl implements ChatService {

    private static final double MIN_CONFIDENCE = 0.5;
    private static final AgentTypeEnum DEFAULT_AGENT = AgentTypeEnum.CONSULT;

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        var routeAgent = this.findAgentByType(AgentTypeEnum.ROUTE);
        var routeResult = routeAgent.process(question, sessionId);

        var agentType = parseRouteResult(routeResult);
        var agent = this.findAgentByType(agentType);
        if (null == agent) {
            log.warn("未找到匹配的智能体，路由结果: {}，使用默认智能体: {}", routeResult, DEFAULT_AGENT);
            var defaultAgent = this.findAgentByType(DEFAULT_AGENT);
            if (defaultAgent != null) {
                return defaultAgent.processStream(question, sessionId);
            }
            return Flux.just(ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .eventData("抱歉，暂时无法处理您的问题，请稍后再试。")
                    .build(), AbstractAgent.STOP_EVENT);
        }
        return agent.processStream(question, sessionId);
    }

    private AgentTypeEnum parseRouteResult(String routeResult) {
        if (StrUtil.isBlank(routeResult)) {
            log.warn("路由结果为空，使用默认智能体: {}", DEFAULT_AGENT);
            return DEFAULT_AGENT;
        }

        String cleaned = routeResult.trim();
        cleaned = extractJson(cleaned);

        try {
            JSONObject json = JSONUtil.parseObj(cleaned);
            String agentName = json.getStr("agentName");
            Double confidence = json.getDouble("confidence", 0.0);
            String reason = json.getStr("reason", "");

            if (StrUtil.isBlank(agentName)) {
                log.warn("路由结果缺少 agentName，使用默认智能体: {}", DEFAULT_AGENT);
                return DEFAULT_AGENT;
            }

            AgentTypeEnum agentType = AgentTypeEnum.agentNameOf(agentName.trim().toUpperCase());
            if (agentType == null) {
                log.warn("路由结果 agentName 不匹配: {}，使用默认智能体: {}", agentName, DEFAULT_AGENT);
                return DEFAULT_AGENT;
            }

            if (confidence < MIN_CONFIDENCE) {
                log.warn("路由置信度过低: {} < {}，agentName: {}，使用默认智能体: {}",
                        confidence, MIN_CONFIDENCE, agentName, DEFAULT_AGENT);
                return DEFAULT_AGENT;
            }

            log.info("路由成功: agentType={}, confidence={}, reason={}", agentType, confidence, reason);
            return agentType;
        } catch (Exception e) {
            log.warn("路由结果解析失败: {}，错误: {}，尝试直接匹配...", routeResult, e.getMessage());
            AgentTypeEnum directMatch = AgentTypeEnum.agentNameOf(routeResult.trim().toUpperCase());
            if (directMatch != null) {
                log.info("直接匹配成功: {}", directMatch);
                return directMatch;
            }
            log.warn("直接匹配也失败，使用默认智能体: {}", DEFAULT_AGENT);
            return DEFAULT_AGENT;
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private Agent findAgentByType(AgentTypeEnum agentTypeEnum) {
        if (null == agentTypeEnum) {
            return null;
        }

        var agents = SpringUtil.getBeansOfType(Agent.class);

        for (Agent agent : agents.values()) {
            if (agent.getAgentType() == agentTypeEnum) {
                return agent;
            }
        }

        return null;
    }

    @Override
    public void stop(String sessionId) {
        var routeAgent = this.findAgentByType(AgentTypeEnum.ROUTE);
        if (routeAgent != null) {
            routeAgent.stop(sessionId);
        }
    }

    @Override
    public String chatText(String question) {
        return "";
    }
}
