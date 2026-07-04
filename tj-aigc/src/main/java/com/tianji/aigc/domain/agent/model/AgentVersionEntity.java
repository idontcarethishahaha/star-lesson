package com.tianji.aigc.domain.agent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.tianji.aigc.domain.agent.constant.PublishStatus;
import com.tianji.aigc.infrastructure.converter.ListConverter;
import com.tianji.aigc.infrastructure.converter.MapConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TableName(value = "agent_versions", autoResultMap = true)
public class AgentVersionEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("agent_id")
    private String agentId;

    @TableField("name")
    private String name;

    @TableField("avatar")
    private String avatar;

    @TableField("description")
    private String description;

    @TableField("version_number")
    private String versionNumber;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("welcome_message")
    private String welcomeMessage;

    @TableField(value = "tool_ids", typeHandler = ListConverter.class)
    private List<String> toolIds;

    @TableField(value = "knowledge_base_ids", typeHandler = ListConverter.class)
    private List<String> knowledgeBaseIds;

    @TableField("change_log")
    private String changeLog;

    @TableField("publish_status")
    private Integer publishStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("user_id")
    private String userId;

    @TableField(value = "tool_preset_params", typeHandler = MapConverter.class)
    private Map<String, Map<String, Map<String, String>>> toolPresetParams;

    @TableField("multi_modal")
    private Boolean multiModal;

    @TableField("llm_model_config")
    private String llmModelConfig;

    public AgentVersionEntity() {
        this.toolIds = new ArrayList<>();
        this.knowledgeBaseIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public List<String> getToolIds() {
        return toolIds != null ? toolIds : new ArrayList<>();
    }

    public void setToolIds(List<String> toolIds) {
        this.toolIds = toolIds;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds != null ? knowledgeBaseIds : new ArrayList<>();
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PublishStatus getPublishStatusEnum() {
        return PublishStatus.fromCode(this.publishStatus);
    }

    public void updatePublishStatus(PublishStatus status) {
        this.publishStatus = status.getCode();
        this.reviewTime = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.publishStatus = PublishStatus.REJECTED.getCode();
        this.rejectReason = reason;
        this.reviewTime = LocalDateTime.now();
    }

    public static AgentVersionEntity createFromAgent(AgentEntity agent, String versionNumber, String changeLog) {
        AgentVersionEntity version = new AgentVersionEntity();
        version.setAgentId(agent.getId());
        version.setName(agent.getName());
        version.setAvatar(agent.getAvatar());
        version.setDescription(agent.getDescription());
        version.setVersionNumber(versionNumber);
        version.setSystemPrompt(agent.getSystemPrompt());
        version.setWelcomeMessage(agent.getWelcomeMessage());
        version.setToolIds(agent.getToolIds());
        version.setKnowledgeBaseIds(agent.getKnowledgeBaseIds());
        version.setChangeLog(changeLog);
        version.setUserId(agent.getUserId());

        LocalDateTime now = LocalDateTime.now();
        version.setCreatedAt(now);
        version.setUpdatedAt(now);
        version.setPublishedAt(now);

        version.setPublishStatus(PublishStatus.REVIEWING.getCode());
        version.setReviewTime(now);
        version.setToolPresetParams(agent.getToolPresetParams());
        return version;
    }

    public Map<String, Map<String, Map<String, String>>> getToolPresetParams() {
        return toolPresetParams;
    }

    public void setToolPresetParams(Map<String, Map<String, Map<String, String>>> toolPresetParams) {
        this.toolPresetParams = toolPresetParams;
    }

    public Boolean getMultiModal() {
        return multiModal;
    }

    public void setMultiModal(Boolean multiModal) {
        this.multiModal = multiModal;
    }

    public String getLlmModelConfig() {
        return llmModelConfig;
    }

    public void setLlmModelConfig(String llmModelConfig) {
        this.llmModelConfig = llmModelConfig;
    }
}
