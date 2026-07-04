package com.tianji.aigc.application.agent.dto;

import lombok.Data;

@Data
public class AgentVersionPublishRequest {

    private String versionId;

    private String changeLog;
}
