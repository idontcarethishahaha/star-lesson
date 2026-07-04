package com.tianji.aigc.domain.agent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName(value = "agent_scheduled_tasks", autoResultMap = true)
public class AgentScheduledTaskEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("agent_id")
    private String agentId;

    @TableField("task_name")
    private String taskName;

    @TableField("task_type")
    private String taskType;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("message_template")
    private String messageTemplate;

    @TableField("target_course_id")
    private Long targetCourseId;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("last_fire_time")
    private LocalDateTime lastFireTime;

    @TableField("next_fire_time")
    private LocalDateTime nextFireTime;

    @TableField("fire_count")
    private Integer fireCount;

    @TableField("remark")
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public Long getTargetCourseId() {
        return targetCourseId;
    }

    public void setTargetCourseId(Long targetCourseId) {
        this.targetCourseId = targetCourseId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastFireTime() {
        return lastFireTime;
    }

    public void setLastFireTime(LocalDateTime lastFireTime) {
        this.lastFireTime = lastFireTime;
    }

    public LocalDateTime getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(LocalDateTime nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Integer getFireCount() {
        return fireCount;
    }

    public void setFireCount(Integer fireCount) {
        this.fireCount = fireCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
