package com.tianji.aigc.application.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.tool.assembler.ToolAssembler;
import com.tianji.aigc.application.tool.dto.*;
import com.tianji.aigc.domain.tool.constant.ToolStatus;
import com.tianji.aigc.domain.tool.constant.ToolType;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import com.tianji.aigc.domain.tool.model.ToolVersionEntity;
import com.tianji.aigc.domain.tool.model.UserToolEntity;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.exception.EntityNotFoundException;
import com.tianji.aigc.mapper.ToolMapper;
import com.tianji.aigc.mapper.ToolVersionMapper;
import com.tianji.aigc.mapper.UserToolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolAppService {

    private final ToolMapper toolMapper;
    private final ToolVersionMapper toolVersionMapper;
    private final UserToolMapper userToolMapper;

    @Transactional
    public String createTool(ToolCreateRequest request, String userId) {
        ToolEntity entity = new ToolEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setName(request.getName());
        entity.setIcon(request.getIcon());
        entity.setSubtitle(request.getSubtitle());
        entity.setDescription(request.getDescription());
        entity.setUserId(userId);
        entity.setLabels(request.getLabels());
        entity.setToolType(request.getToolType());
        entity.setUploadType(request.getUploadType());
        entity.setUploadUrl(request.getUploadUrl());
        entity.setInstallCommand(request.getInstallCommand());
        entity.setIsGlobal(request.getIsGlobal());
        entity.setStatus(ToolStatus.DRAFT);
        toolMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateTool(String id, ToolUpdateRequest request) {
        ToolEntity entity = toolMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("工具不存在");
        }
        ToolAssembler.updateEntity(entity, request);
        toolMapper.updateById(entity);
    }

    @Transactional
    public void deleteTool(String id) {
        ToolEntity entity = toolMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("工具不存在");
        }

        LambdaQueryWrapper<ToolVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ToolVersionEntity::getToolId, id);
        toolVersionMapper.delete(versionWrapper);

        LambdaQueryWrapper<UserToolEntity> userToolWrapper = new LambdaQueryWrapper<>();
        userToolWrapper.eq(UserToolEntity::getToolId, id);
        userToolMapper.delete(userToolWrapper);

        toolMapper.deleteById(id);
    }

    public ToolDTO getToolById(String id) {
        return getToolById(id, null);
    }

    public ToolDTO getToolById(String id, String userId) {
        ToolEntity entity = toolMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("工具不存在");
        }
        ToolDTO dto = ToolAssembler.toDTO(entity);

        if (userId != null) {
            LambdaQueryWrapper<UserToolEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserToolEntity::getToolId, id);
            wrapper.eq(UserToolEntity::getUserId, userId);
            UserToolEntity userTool = userToolMapper.selectOne(wrapper);
            dto.setInstalled(userTool != null);
            dto.setInstalledVersion(userTool != null ? userTool.getVersion() : null);
        }

        LambdaQueryWrapper<ToolVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ToolVersionEntity::getToolId, id);
        dto.setVersionCount(Math.toIntExact(toolVersionMapper.selectCount(versionWrapper)));

        return dto;
    }

    public PageDTO<ToolDTO> searchTools(ToolSearchRequest request) {
        return searchTools(request, null);
    }

    public PageDTO<ToolDTO> searchTools(ToolSearchRequest request, String userId) {
        Page<ToolEntity> page = new Page<>(request.getPageNo(), request.getPageSize());
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();

        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(ToolEntity::getName, request.getName());
        }
        if (request.getUserId() != null) {
            wrapper.eq(ToolEntity::getUserId, request.getUserId());
        }
        if (request.getIsGlobal() != null) {
            wrapper.eq(ToolEntity::getIsGlobal, request.getIsGlobal());
        }
        if (request.getToolType() != null && !request.getToolType().isEmpty()) {
            wrapper.eq(ToolEntity::getToolType, ToolType.valueOf(request.getToolType()));
        }

        wrapper.and(w -> w
                .eq(ToolEntity::getStatus, ToolStatus.APPROVED)
                .or()
                .eq(ToolEntity::getIsGlobal, true)
        );

        wrapper.orderByDesc(ToolEntity::getCreatedAt);

        Page<ToolEntity> result = toolMapper.selectPage(page, wrapper);

        Set<String> installedToolIds;
        if (userId != null) {
            LambdaQueryWrapper<UserToolEntity> userToolWrapper = new LambdaQueryWrapper<>();
            userToolWrapper.eq(UserToolEntity::getUserId, userId);
            List<UserToolEntity> userTools = userToolMapper.selectList(userToolWrapper);
            installedToolIds = userTools.stream()
                    .map(UserToolEntity::getToolId)
                    .collect(Collectors.toSet());
        } else {
            installedToolIds = new HashSet<>();
        }

        PageDTO<ToolDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(entity -> {
                    ToolDTO dto = ToolAssembler.toDTO(entity);
                    dto.setInstalled(installedToolIds.contains(entity.getId()));

                    LambdaQueryWrapper<ToolVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
                    versionWrapper.eq(ToolVersionEntity::getToolId, entity.getId());
                    dto.setVersionCount(Math.toIntExact(toolVersionMapper.selectCount(versionWrapper)));

                    return dto;
                })
                .collect(Collectors.toList()));
        return pageDTO;
    }

    public List<ToolDTO> listMyTools(String userId) {
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getUserId, userId);
        wrapper.orderByDesc(ToolEntity::getCreatedAt);
        return toolMapper.selectList(wrapper).stream()
                .map(ToolAssembler::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String installTool(ToolInstallRequest request, String userId) {
        ToolEntity tool = toolMapper.selectById(request.getToolId());
        if (tool == null) {
            throw new EntityNotFoundException("工具不存在");
        }

        LambdaQueryWrapper<UserToolEntity> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(UserToolEntity::getToolId, request.getToolId());
        existingWrapper.eq(UserToolEntity::getUserId, userId);
        if (userToolMapper.selectOne(existingWrapper) != null) {
            throw new BusinessException("该工具已安装");
        }

        ToolVersionEntity version = null;
        if (request.getVersion() != null && !request.getVersion().isEmpty()) {
            LambdaQueryWrapper<ToolVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
            versionWrapper.eq(ToolVersionEntity::getToolId, request.getToolId());
            versionWrapper.eq(ToolVersionEntity::getVersion, request.getVersion());
            version = toolVersionMapper.selectOne(versionWrapper);
        } else {
            LambdaQueryWrapper<ToolVersionEntity> versionWrapper = new LambdaQueryWrapper<>();
            versionWrapper.eq(ToolVersionEntity::getToolId, request.getToolId());
            versionWrapper.orderByDesc(ToolVersionEntity::getCreatedAt);
            versionWrapper.last("LIMIT 1");
            version = toolVersionMapper.selectOne(versionWrapper);
        }

        UserToolEntity userTool = new UserToolEntity();
        userTool.setId(UUID.randomUUID().toString().replace("-", ""));
        userTool.setUserId(userId);
        userTool.setToolId(request.getToolId());
        userTool.setName(tool.getName());
        userTool.setDescription(tool.getDescription());
        userTool.setIcon(tool.getIcon());
        userTool.setSubtitle(tool.getSubtitle());
        userTool.setLabels(tool.getLabels());
        userTool.setMcpServerName(tool.getMcpServerName());
        userTool.setIsGlobal(tool.getIsGlobal());
        userTool.setPublicState(request.getIsPublic());

        if (version != null) {
            userTool.setVersion(version.getVersion());
            userTool.setToolList(version.getToolList());
        } else {
            userTool.setToolList(tool.getToolList());
            userTool.setVersion("1.0.0");
        }

        userToolMapper.insert(userTool);
        return userTool.getId();
    }

    @Transactional
    public void uninstallTool(String toolId, String userId) {
        LambdaQueryWrapper<UserToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToolEntity::getToolId, toolId);
        wrapper.eq(UserToolEntity::getUserId, userId);
        userToolMapper.delete(wrapper);
    }

    public List<ToolDTO> listInstalledTools(String userId) {
        LambdaQueryWrapper<UserToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserToolEntity::getUserId, userId);
        return userToolMapper.selectList(wrapper).stream()
                .map(this::userToolToDTO)
                .collect(Collectors.toList());
    }

    public List<ToolDTO> listUserToolsByIds(List<String> toolIds, String userId) {
        if (toolIds == null || toolIds.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<UserToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserToolEntity::getToolId, toolIds);
        wrapper.eq(UserToolEntity::getUserId, userId);
        return userToolMapper.selectList(wrapper).stream()
                .map(this::userToolToDTO)
                .collect(Collectors.toList());
    }

    private ToolDTO userToolToDTO(UserToolEntity entity) {
        ToolDTO dto = new ToolDTO();
        dto.setId(entity.getId());
        dto.setToolId(entity.getToolId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setSubtitle(entity.getSubtitle());
        dto.setDescription(entity.getDescription());
        dto.setLabels(entity.getLabels());
        dto.setToolList(entity.getToolList());
        dto.setVersion(entity.getVersion());
        dto.setMcpServerName(entity.getMcpServerName());
        dto.setIsGlobal(entity.getIsGlobal());
        dto.setInstalled(true);
        dto.setInstalledVersion(entity.getVersion());
        dto.setPublicState(entity.getPublicState());
        return dto;
    }

    @Transactional
    public String createVersion(ToolVersionCreateRequest request, String userId) {
        ToolEntity tool = toolMapper.selectById(request.getToolId());
        if (tool == null) {
            throw new EntityNotFoundException("工具不存在");
        }

        ToolVersionEntity version = new ToolVersionEntity();
        version.setId(UUID.randomUUID().toString().replace("-", ""));
        version.setName(request.getName() != null ? request.getName() : tool.getName());
        version.setIcon(request.getIcon() != null ? request.getIcon() : tool.getIcon());
        version.setSubtitle(request.getSubtitle() != null ? request.getSubtitle() : tool.getSubtitle());
        version.setDescription(request.getDescription() != null ? request.getDescription() : tool.getDescription());
        version.setUserId(userId);
        version.setVersion(request.getVersion());
        version.setToolId(request.getToolId());
        version.setUploadUrl(request.getUploadUrl());
        version.setLabels(tool.getLabels());
        version.setMcpServerName(request.getMcpServerName() != null ? request.getMcpServerName() : tool.getMcpServerName());
        version.setToolList(tool.getToolList());
        version.setChangeLog(request.getChangeLog());

        toolVersionMapper.insert(version);
        return version.getId();
    }

    public List<ToolVersionDTO> listVersions(String toolId) {
        LambdaQueryWrapper<ToolVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolVersionEntity::getToolId, toolId);
        wrapper.orderByDesc(ToolVersionEntity::getCreatedAt);
        return toolVersionMapper.selectList(wrapper).stream()
                .map(ToolAssembler::toVersionDTO)
                .collect(Collectors.toList());
    }

    public ToolVersionDTO getVersion(String toolId, String versionId) {
        ToolVersionEntity version = toolVersionMapper.selectById(versionId);
        if (version == null || !version.getToolId().equals(toolId)) {
            throw new EntityNotFoundException("版本不存在");
        }
        return ToolAssembler.toVersionDTO(version);
    }

    public List<ToolDefinition> getToolDefinitions(String userId, List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ToolDefinition> allDefinitions = new ArrayList<>();

        LambdaQueryWrapper<UserToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserToolEntity::getToolId, toolIds);
        wrapper.eq(UserToolEntity::getUserId, userId);
        List<UserToolEntity> userTools = userToolMapper.selectList(wrapper);

        for (UserToolEntity userTool : userTools) {
            if (userTool.getToolList() != null) {
                allDefinitions.addAll(userTool.getToolList());
            }
        }

        return allDefinitions;
    }
}
