package com.tianji.aigc.application.agent.dto;

import com.tianji.aigc.domain.agent.constant.AgentStatus;
import com.tianji.aigc.domain.agent.constant.PublishStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentDTO {

    private String id;

    private String name;

    private String avatar;

    private String description;

    private String systemPrompt;

    private String welcomeMessage;

    private List<String> toolIds;

    private List<String> knowledgeBaseIds;

    private String publishedVersion;

    private Boolean enabled;

    private String userId;

    private Boolean multiModal;

    private String llmModelConfig;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer versionCount;

    private PublishStatus publishStatus;
}
