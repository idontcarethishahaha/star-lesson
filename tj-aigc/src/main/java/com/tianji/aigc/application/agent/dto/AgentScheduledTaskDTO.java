package com.tianji.aigc.application.agent.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentScheduledTaskDTO {

    private String id;

    private String userId;

    private String agentId;

    private String taskName;

    private String taskType;

    private String taskTypeName;

    private String cronExpression;

    private String messageTemplate;

    private Long targetCourseId;

    private String targetCourseName;

    private Boolean enabled;

    private LocalDateTime lastFireTime;

    private LocalDateTime nextFireTime;

    private Integer fireCount;

    private String remark;

    private LocalDateTime createdAt;
}
