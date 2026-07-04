package com.tianji.aigc.application.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentBaseRequest {

    private String name;

    private String avatar;

    private String description;

    private String systemPrompt;

    private String welcomeMessage;

    private List<String> toolIds;

    private List<String> knowledgeBaseIds;

    private String llmModelConfig;

    private Boolean multiModal = false;
}
