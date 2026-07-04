package com.tianji.aigc.application.tool.assembler;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.application.tool.dto.ToolDTO;
import com.tianji.aigc.application.tool.dto.ToolVersionDTO;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import com.tianji.aigc.domain.tool.model.ToolVersionEntity;

public class ToolAssembler {

    public static ToolDTO toDTO(ToolEntity entity) {
        if (entity == null) {
            return null;
        }
        ToolDTO dto = new ToolDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setSubtitle(entity.getSubtitle());
        dto.setDescription(entity.getDescription());
        dto.setUserId(entity.getUserId());
        dto.setLabels(entity.getLabels());
        dto.setToolType(entity.getToolType());
        dto.setUploadType(entity.getUploadType());
        dto.setUploadUrl(entity.getUploadUrl());
        dto.setInstallCommand(entity.getInstallCommand());
        dto.setToolList(entity.getToolList());
        dto.setStatus(entity.getStatus());
        dto.setIsOffice(entity.getIsOffice());
        dto.setRejectReason(entity.getRejectReason());
        dto.setMcpServerName(entity.getMcpServerName());
        dto.setIsGlobal(entity.getIsGlobal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static void updateEntity(ToolEntity entity, com.tianji.aigc.application.tool.dto.ToolUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getSubtitle() != null) {
            entity.setSubtitle(request.getSubtitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getLabels() != null) {
            entity.setLabels(request.getLabels());
        }
        if (request.getUploadType() != null) {
            entity.setUploadType(request.getUploadType());
        }
        if (request.getUploadUrl() != null) {
            entity.setUploadUrl(request.getUploadUrl());
        }
        if (request.getInstallCommand() != null) {
            entity.setInstallCommand(request.getInstallCommand());
        }
        if (request.getIsGlobal() != null) {
            entity.setIsGlobal(request.getIsGlobal());
        }
    }

    public static ToolVersionDTO toVersionDTO(ToolVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        ToolVersionDTO dto = new ToolVersionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setSubtitle(entity.getSubtitle());
        dto.setDescription(entity.getDescription());
        dto.setUserId(entity.getUserId());
        dto.setVersion(entity.getVersion());
        dto.setToolId(entity.getToolId());
        dto.setUploadUrl(entity.getUploadUrl());
        dto.setToolList(entity.getToolList());
        dto.setLabels(entity.getLabels());
        dto.setIsOffice(entity.getIsOffice());
        dto.setPublicStatus(entity.getPublicStatus());
        dto.setChangeLog(entity.getChangeLog());
        dto.setMcpServerName(entity.getMcpServerName());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        return dto;
    }
}
