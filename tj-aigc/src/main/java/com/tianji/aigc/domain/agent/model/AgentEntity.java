package com.tianji.aigc.domain.agent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.tianji.aigc.infrastructure.converter.ListStringConverter;
import com.tianji.aigc.infrastructure.converter.MapConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TableName(value = "agents", autoResultMap = true)
public class AgentEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("name")
    private String name;

    @TableField("avatar")
    private String avatar;

    @TableField("description")
    private String description;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("welcome_message")
    private String welcomeMessage;

    @TableField(value = "tool_ids", typeHandler = ListStringConverter.class)
    private List<String> toolIds;

    @TableField(value = "knowledge_base_ids", typeHandler = ListStringConverter.class)
    private List<String> knowledgeBaseIds;

    @TableField("published_version")
    private String publishedVersion;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("user_id")
    private String userId;

    @TableField(value = "tool_preset_params", typeHandler = MapConverter.class)
    private Map<String, Map<String, Map<String, String>>> toolPresetParams;

    @TableField("multi_modal")
    private Boolean multiModal;

    @TableField("llm_model_config")
    private String llmModelConfig;

    public AgentEntity() {
        this.toolIds = new ArrayList<>();
        this.knowledgeBaseIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPublishedVersion() {
        return publishedVersion;
    }

    public void setPublishedVersion(String publishedVersion) {
        this.publishedVersion = publishedVersion;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static AgentEntity createNew(String name, String description, String avatar, String userId) {
        AgentEntity agent = new AgentEntity();
        agent.setName(name);
        agent.setDescription(description);
        agent.setAvatar(avatar);
        agent.setUserId(userId);
        agent.setEnabled(true);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        return agent;
    }

    public void updateBasicInfo(String name, String avatar, String description) {
        this.name = name;
        this.avatar = avatar;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void publishVersion(String versionId) {
        this.publishedVersion = versionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void isEnable() {
        if (!this.enabled) {
            throw new BusinessException("助理未激活");
        }
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
