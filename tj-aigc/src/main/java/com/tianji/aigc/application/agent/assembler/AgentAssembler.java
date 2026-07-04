package com.tianji.aigc.application.agent.assembler;

import com.tianji.aigc.application.agent.dto.*;
import com.tianji.aigc.domain.agent.model.AgentEntity;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import org.springframework.beans.BeanUtils;

public class AgentAssembler {

    public static AgentDTO toDTO(AgentEntity entity) {
        if (entity == null) {
            return null;
        }
        AgentDTO dto = new AgentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAvatar(entity.getAvatar());
        dto.setDescription(entity.getDescription());
        dto.setSystemPrompt(entity.getSystemPrompt());
        dto.setWelcomeMessage(entity.getWelcomeMessage());
        dto.setToolIds(entity.getToolIds());
        dto.setKnowledgeBaseIds(entity.getKnowledgeBaseIds());
        dto.setPublishedVersion(entity.getPublishedVersion());
        dto.setEnabled(entity.getEnabled());
        dto.setUserId(entity.getUserId());
        dto.setMultiModal(entity.getMultiModal());
        dto.setLlmModelConfig(entity.getLlmModelConfig());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static AgentEntity toEntity(AgentCreateRequest request, String userId) {
        AgentEntity entity = new AgentEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(generateId());
        entity.setUserId(userId);
        entity.setEnabled(true);
        return entity;
    }

    public static void updateEntity(AgentEntity entity, AgentUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            entity.setAvatar(request.getAvatar());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getSystemPrompt() != null) {
            entity.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getWelcomeMessage() != null) {
            entity.setWelcomeMessage(request.getWelcomeMessage());
        }
        if (request.getToolIds() != null) {
            entity.setToolIds(request.getToolIds());
        }
        if (request.getKnowledgeBaseIds() != null) {
            entity.setKnowledgeBaseIds(request.getKnowledgeBaseIds());
        }
        if (request.getLlmModelConfig() != null) {
            entity.setLlmModelConfig(request.getLlmModelConfig());
        }
        if (request.getMultiModal() != null) {
            entity.setMultiModal(request.getMultiModal());
        }
    }

    public static AgentVersionDTO toVersionDTO(AgentVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        AgentVersionDTO dto = new AgentVersionDTO();
        dto.setId(entity.getId());
        dto.setAgentId(entity.getAgentId());
        dto.setName(entity.getName());
        dto.setAvatar(entity.getAvatar());
        dto.setDescription(entity.getDescription());
        dto.setVersionNumber(entity.getVersionNumber());
        dto.setSystemPrompt(entity.getSystemPrompt());
        dto.setWelcomeMessage(entity.getWelcomeMessage());
        dto.setToolIds(entity.getToolIds());
        dto.setKnowledgeBaseIds(entity.getKnowledgeBaseIds());
        dto.setChangeLog(entity.getChangeLog());
        dto.setPublishStatus(entity.getPublishStatusEnum() != null ? entity.getPublishStatusEnum().getCode() : null);
        dto.setRejectReason(entity.getRejectReason());
        dto.setReviewTime(entity.getReviewTime());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setUserId(entity.getUserId());
        dto.setLlmModelConfig(entity.getLlmModelConfig());
        dto.setMultiModal(entity.getMultiModal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static AgentVersionEntity toVersionEntity(AgentVersionCreateRequest request, String agentId, String userId) {
        AgentVersionEntity entity = new AgentVersionEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(generateId());
        entity.setAgentId(agentId);
        entity.setUserId(userId);
        entity.setPublishStatus(com.tianji.aigc.domain.agent.constant.PublishStatus.REVIEWING.getCode());
        return entity;
    }

    public static void updateVersionEntity(AgentVersionEntity entity, AgentVersionUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            entity.setAvatar(request.getAvatar());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getVersionNumber() != null) {
            entity.setVersionNumber(request.getVersionNumber());
        }
        if (request.getSystemPrompt() != null) {
            entity.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getWelcomeMessage() != null) {
            entity.setWelcomeMessage(request.getWelcomeMessage());
        }
        if (request.getToolIds() != null) {
            entity.setToolIds(request.getToolIds());
        }
        if (request.getKnowledgeBaseIds() != null) {
            entity.setKnowledgeBaseIds(request.getKnowledgeBaseIds());
        }
        if (request.getChangeLog() != null) {
            entity.setChangeLog(request.getChangeLog());
        }
        if (request.getLlmModelConfig() != null) {
            entity.setLlmModelConfig(request.getLlmModelConfig());
        }
        if (request.getMultiModal() != null) {
            entity.setMultiModal(request.getMultiModal());
        }
    }

    private static String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
