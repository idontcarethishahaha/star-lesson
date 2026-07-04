package com.tianji.aigc.application.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentVersionBaseRequest {

    private String name;

    private String avatar;

    private String description;

    private String versionNumber;

    private String systemPrompt;

    private String welcomeMessage;

    private List<String> toolIds;

    private List<String> knowledgeBaseIds;

    private String changeLog;

    private String llmModelConfig;

    private Boolean multiModal = false;
}
