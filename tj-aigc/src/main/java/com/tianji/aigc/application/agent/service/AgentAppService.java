package com.tianji.aigc.application.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.aigc.application.agent.assembler.AgentAssembler;
import com.tianji.aigc.application.agent.dto.*;
import com.tianji.aigc.domain.agent.constant.AgentStatus;
import com.tianji.aigc.domain.agent.constant.PublishStatus;
import com.tianji.aigc.domain.agent.model.AgentEntity;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.exception.EntityNotFoundException;
import com.tianji.aigc.mapper.AgentMapper;
import com.tianji.aigc.mapper.AgentVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentAppService {

    private final AgentMapper agentMapper;
    private final AgentVersionMapper agentVersionMapper;

    @Transactional
    public String createAgent(AgentCreateRequest request, String userId) {
        AgentEntity entity = AgentAssembler.toEntity(request, userId);
        agentMapper.insert(entity);

        AgentVersionCreateRequest versionRequest = new AgentVersionCreateRequest();
        versionRequest.setName(request.getName());
        versionRequest.setAvatar(request.getAvatar());
        versionRequest.setDescription(request.getDescription());
        versionRequest.setSystemPrompt(request.getSystemPrompt());
        versionRequest.setWelcomeMessage(request.getWelcomeMessage());
        versionRequest.setVersionNumber("1.0.0");
        versionRequest.setToolIds(request.getToolIds());
        versionRequest.setKnowledgeBaseIds(request.getKnowledgeBaseIds());
        versionRequest.setLlmModelConfig(request.getLlmModelConfig());
        versionRequest.setMultiModal(request.getMultiModal());

        AgentVersionEntity version = AgentAssembler.toVersionEntity(versionRequest, entity.getId(), userId);
        agentVersionMapper.insert(version);

        entity.setPublishedVersion(version.getId());
        agentMapper.updateById(entity);

        return entity.getId();
    }

    @Transactional
    public void updateAgent(String id, AgentUpdateRequest request) {
        AgentEntity entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        AgentAssembler.updateEntity(entity, request);
        agentMapper.updateById(entity);
    }

    @Transactional
    public void deleteAgent(String id) {
        AgentEntity entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        entity.setDeletedAt(LocalDateTime.now());
        agentMapper.updateById(entity);

        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentId, id);
        List<AgentVersionEntity> versions = agentVersionMapper.selectList(wrapper);
        for (AgentVersionEntity version : versions) {
            version.setDeletedAt(LocalDateTime.now());
            agentVersionMapper.updateById(version);
        }
    }

    public AgentDTO getAgentById(String id) {
        AgentEntity entity = agentMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        AgentDTO dto = AgentAssembler.toDTO(entity);

        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentId, id);
        wrapper.isNull(AgentVersionEntity::getDeletedAt);
        dto.setVersionCount(Math.toIntExact(agentVersionMapper.selectCount(wrapper)));

        if (entity.getPublishedVersion() != null) {
            AgentVersionEntity publishedVersion = agentVersionMapper.selectById(entity.getPublishedVersion());
            if (publishedVersion != null) {
                dto.setPublishStatus(PublishStatus.fromCode(publishedVersion.getPublishStatus()));
            }
        }

        return dto;
    }

    public PageDTO<AgentDTO> listAgents(AgentSearchRequest request) {
        Page<AgentEntity> page = new Page<>(request.getPageNo(), request.getPageSize());
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(AgentEntity::getDeletedAt);
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(AgentEntity::getName, request.getName());
        }
        if (request.getUserId() != null) {
            wrapper.eq(AgentEntity::getUserId, request.getUserId());
        }
        if (request.getEnabled() != null) {
            wrapper.eq(AgentEntity::getEnabled, request.getEnabled());
        }
        wrapper.orderByDesc(AgentEntity::getCreatedAt);

        Page<AgentEntity> result = agentMapper.selectPage(page, wrapper);
        PageDTO<AgentDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(AgentAssembler::toDTO)
                .collect(Collectors.toList()));
        return pageDTO;
    }

    @Transactional
    public String createAgentVersion(String agentId, AgentVersionCreateRequest request, String userId) {
        AgentEntity agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        AgentVersionEntity entity = AgentAssembler.toVersionEntity(request, agentId, userId);
        agentVersionMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateAgentVersion(String agentId, String versionId, AgentVersionUpdateRequest request) {
        AgentVersionEntity entity = agentVersionMapper.selectById(versionId);
        if (entity == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        if (!entity.getAgentId().equals(agentId)) {
            throw new BusinessException("版本不属于该Agent");
        }
        if (entity.getPublishStatus() != null && entity.getPublishStatus().equals(PublishStatus.PUBLISHED.getCode())) {
            throw new BusinessException("已发布的版本不允许修改");
        }
        AgentAssembler.updateVersionEntity(entity, request);
        agentVersionMapper.updateById(entity);
    }

    @Transactional
    public void deleteAgentVersion(String agentId, String versionId) {
        AgentVersionEntity entity = agentVersionMapper.selectById(versionId);
        if (entity == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        if (!entity.getAgentId().equals(agentId)) {
            throw new BusinessException("版本不属于该Agent");
        }
        if (entity.getPublishStatus() != null && entity.getPublishStatus().equals(PublishStatus.PUBLISHED.getCode())) {
            throw new BusinessException("已发布的版本不允许删除");
        }
        entity.setDeletedAt(LocalDateTime.now());
        agentVersionMapper.updateById(entity);
    }

    public AgentVersionDTO getAgentVersion(String agentId, String versionId) {
        AgentVersionEntity entity = agentVersionMapper.selectById(versionId);
        if (entity == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        if (!entity.getAgentId().equals(agentId)) {
            throw new BusinessException("版本不属于该Agent");
        }
        return AgentAssembler.toVersionDTO(entity);
    }

    public List<AgentVersionDTO> listAgentVersions(String agentId) {
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentId, agentId);
        wrapper.isNull(AgentVersionEntity::getDeletedAt);
        wrapper.orderByDesc(AgentVersionEntity::getCreatedAt);
        return agentVersionMapper.selectList(wrapper).stream()
                .map(AgentAssembler::toVersionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void publishAgentVersion(String agentId, AgentVersionPublishRequest request) {
        AgentVersionEntity entity = agentVersionMapper.selectById(request.getVersionId());
        if (entity == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        if (!entity.getAgentId().equals(agentId)) {
            throw new BusinessException("版本不属于该Agent");
        }
        if (entity.getSystemPrompt() == null || entity.getSystemPrompt().isEmpty()) {
            throw new BusinessException("系统提示词不能为空");
        }
        entity.setPublishStatus(PublishStatus.PUBLISHED.getCode());
        entity.setPublishedAt(LocalDateTime.now());
        if (request.getChangeLog() != null) {
            entity.setChangeLog(request.getChangeLog());
        }
        agentVersionMapper.updateById(entity);

        AgentEntity agent = agentMapper.selectById(agentId);
        if (agent != null) {
            agent.setPublishedVersion(entity.getId());
            agent.setSystemPrompt(entity.getSystemPrompt());
            agent.setWelcomeMessage(entity.getWelcomeMessage());
            agent.setToolIds(entity.getToolIds());
            agent.setKnowledgeBaseIds(entity.getKnowledgeBaseIds());
            agent.setLlmModelConfig(entity.getLlmModelConfig());
            agent.setMultiModal(entity.getMultiModal());
            agent.setToolPresetParams(entity.getToolPresetParams());
            agentMapper.updateById(agent);
        }
    }

    @Transactional
    public void offlineAgentVersion(String agentId) {
        AgentEntity agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        if (agent.getPublishedVersion() != null) {
            AgentVersionEntity version = agentVersionMapper.selectById(agent.getPublishedVersion());
            if (version != null) {
                version.setPublishStatus(PublishStatus.REMOVED.getCode());
                agentVersionMapper.updateById(version);
            }
        }
        agent.setPublishedVersion(null);
        agentMapper.updateById(agent);
    }

    @Transactional
    public void publishReview(String agentId, String versionId, boolean approved, String reason) {
        AgentVersionEntity entity = agentVersionMapper.selectById(versionId);
        if (entity == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        if (!entity.getAgentId().equals(agentId)) {
            throw new BusinessException("版本不属于该Agent");
        }
        entity.setReviewTime(LocalDateTime.now());
        if (approved) {
            entity.setPublishStatus(PublishStatus.PUBLISHED.getCode());
        } else {
            entity.setPublishStatus(PublishStatus.REJECTED.getCode());
            entity.setRejectReason(reason);
        }
        agentVersionMapper.updateById(entity);
    }

    @Transactional
    public void bindToolsToAgent(String agentId, List<String> toolIds) {
        AgentEntity entity = agentMapper.selectById(agentId);
        if (entity == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        entity.setToolIds(toolIds);
        agentMapper.updateById(entity);
    }

    @Transactional
    public void bindKnowledgeBasesToAgent(String agentId, List<String> kbIds) {
        AgentEntity entity = agentMapper.selectById(agentId);
        if (entity == null) {
            throw new EntityNotFoundException("Agent不存在");
        }
        entity.setKnowledgeBaseIds(kbIds);
        agentMapper.updateById(entity);
    }
}
