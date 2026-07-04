package com.tianji.aigc.application.agent.dto;

import com.tianji.aigc.domain.agent.constant.PublishStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentVersionDTO {

    private String id;

    private String agentId;

    private String name;

    private String avatar;

    private String description;

    private String versionNumber;

    private String systemPrompt;

    private String welcomeMessage;

    private List<String> toolIds;

    private List<String> knowledgeBaseIds;

    private String changeLog;

    private Integer publishStatus;

    private String rejectReason;

    private LocalDateTime reviewTime;

    private LocalDateTime publishedAt;

    private String userId;

    private String llmModelConfig;

    private Boolean multiModal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
