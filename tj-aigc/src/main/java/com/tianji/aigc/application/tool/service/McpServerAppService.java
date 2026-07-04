package com.tianji.aigc.application.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.aigc.application.tool.dto.*;
import com.tianji.aigc.domain.tool.model.McpServerEntity;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import com.tianji.aigc.domain.tool.model.UserToolEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.exception.EntityNotFoundException;
import com.tianji.aigc.mapper.McpServerMapper;
import com.tianji.aigc.mapper.ToolMapper;
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
public class McpServerAppService {

    private final McpServerMapper mcpServerMapper;
    private final ToolMapper toolMapper;
    private final UserToolMapper userToolMapper;

    @Transactional
    public String createServer(McpServerCreateRequest request, String userId) {
        McpServerEntity entity = new McpServerEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setCommand(request.getCommand());
        entity.setArgs(request.getArgs());
        entity.setEnv(request.getEnv());
        entity.setUrl(request.getUrl());
        entity.setDescription(request.getDescription());
        entity.setUserId(userId);
        entity.setIsGlobal(request.getIsGlobal());
        entity.setStatus("stopped");
        mcpServerMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateServer(String id, McpServerUpdateRequest request) {
        McpServerEntity entity = mcpServerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("MCP Server 不存在");
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getType() != null) {
            entity.setType(request.getType());
        }
        if (request.getCommand() != null) {
            entity.setCommand(request.getCommand());
        }
        if (request.getArgs() != null) {
            entity.setArgs(request.getArgs());
        }
        if (request.getEnv() != null) {
            entity.setEnv(request.getEnv());
        }
        if (request.getUrl() != null) {
            entity.setUrl(request.getUrl());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getIsGlobal() != null) {
            entity.setIsGlobal(request.getIsGlobal());
        }

        mcpServerMapper.updateById(entity);
    }

    @Transactional
    public void deleteServer(String id) {
        McpServerEntity entity = mcpServerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("MCP Server 不存在");
        }

        LambdaQueryWrapper<ToolEntity> toolWrapper = new LambdaQueryWrapper<>();
        toolWrapper.eq(ToolEntity::getMcpServerName, entity.getName());
        toolMapper.delete(toolWrapper);

        LambdaQueryWrapper<UserToolEntity> userToolWrapper = new LambdaQueryWrapper<>();
        userToolWrapper.eq(UserToolEntity::getMcpServerName, entity.getName());
        userToolMapper.delete(userToolWrapper);

        mcpServerMapper.deleteById(id);
    }

    public McpServerDTO getServerById(String id) {
        McpServerEntity entity = mcpServerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("MCP Server 不存在");
        }
        return toDTO(entity);
    }

    public List<McpServerDTO> listServers(String userId) {
        LambdaQueryWrapper<McpServerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(McpServerEntity::getUserId, userId)
                .or()
                .eq(McpServerEntity::getIsGlobal, true)
        );
        wrapper.orderByDesc(McpServerEntity::getCreatedAt);
        return mcpServerMapper.selectList(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<McpServerDTO> listMyServers(String userId) {
        LambdaQueryWrapper<McpServerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(McpServerEntity::getUserId, userId);
        wrapper.orderByDesc(McpServerEntity::getCreatedAt);
        return mcpServerMapper.selectList(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void startServer(String id) {
        McpServerEntity entity = mcpServerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("MCP Server 不存在");
        }

        if ("running".equals(entity.getStatus())) {
            throw new BusinessException("MCP Server 已经在运行中");
        }

        log.info("启动 MCP Server: {}, type={}, command={}", entity.getName(), entity.getType(), entity.getCommand());

        entity.setStatus("running");
        mcpServerMapper.updateById(entity);
    }

    @Transactional
    public void stopServer(String id) {
        McpServerEntity entity = mcpServerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("MCP Server 不存在");
        }

        if ("stopped".equals(entity.getStatus())) {
            throw new BusinessException("MCP Server 已经停止");
        }

        log.info("停止 MCP Server: {}", entity.getName());

        entity.setStatus("stopped");
        mcpServerMapper.updateById(entity);
    }

    public List<McpServerDTO> listServersByNames(List<String> names, String userId) {
        if (names == null || names.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<McpServerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(McpServerEntity::getName, names);
        wrapper.and(w -> w
                .eq(McpServerEntity::getUserId, userId)
                .or()
                .eq(McpServerEntity::getIsGlobal, true)
        );
        return mcpServerMapper.selectList(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private McpServerDTO toDTO(McpServerEntity entity) {
        McpServerDTO dto = new McpServerDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setCommand(entity.getCommand());
        dto.setArgs(entity.getArgs());
        dto.setEnv(entity.getEnv());
        dto.setUrl(entity.getUrl());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setUserId(entity.getUserId());
        dto.setIsGlobal(entity.getIsGlobal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        List<String> tools = new ArrayList<>();
        LambdaQueryWrapper<ToolEntity> toolWrapper = new LambdaQueryWrapper<>();
        toolWrapper.eq(ToolEntity::getMcpServerName, entity.getName());
        toolWrapper.eq(ToolEntity::getStatus, com.tianji.aigc.domain.tool.constant.ToolStatus.APPROVED);
        List<ToolEntity> toolEntities = toolMapper.selectList(toolWrapper);
        for (ToolEntity tool : toolEntities) {
            if (tool.getToolList() != null) {
                for (var def : tool.getToolList()) {
                    tools.add(def.getName());
                }
            }
        }
        dto.setTools(tools);

        return dto;
    }
}
