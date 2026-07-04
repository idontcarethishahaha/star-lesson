package com.tianji.aigc.application.agent.dto;

import lombok.Data;

@Data
public class AgentScheduledTaskRequest {

    private String taskName;

    private String taskType;

    private String cronExpression;

    private String messageTemplate;

    private Long targetCourseId;

    private String remark;

    private String agentId;
}
