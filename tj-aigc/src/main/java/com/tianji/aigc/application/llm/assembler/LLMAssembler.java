package com.tianji.aigc.application.llm.assembler;

import com.tianji.aigc.application.llm.dto.*;
import com.tianji.aigc.domain.llm.model.ModelEntity;
import com.tianji.aigc.domain.llm.model.ProviderEntity;

public class LLMAssembler {

    public static ProviderDTO toProviderDTO(ProviderEntity entity) {
        if (entity == null) {
            return null;
        }
        ProviderDTO dto = new ProviderDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setProtocol(entity.getProtocol() != null ? entity.getProtocol().name() : null);
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setConfig(entity.getConfig() != null ? entity.getConfig().toString() : null);
        dto.setIsOfficial(entity.getIsOfficial());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static ProviderEntity toProviderEntity(ProviderCreateRequest request) {
        ProviderEntity entity = new ProviderEntity();
        entity.setId(generateId());
        entity.setProtocol(request.getProtocol() != null
                ? com.tianji.aigc.infrastructure.llm.protocol.enums.ProviderProtocol.valueOf(request.getProtocol())
                : null);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setIsOfficial(request.getIsOfficial());
        entity.setStatus(true);
        return entity;
    }

    public static void updateProviderEntity(ProviderEntity entity, ProviderUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getConfig() != null) {
            // 暂不处理复杂配置转换
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    public static ModelDTO toModelDTO(ModelEntity entity) {
        if (entity == null) {
            return null;
        }
        ModelDTO dto = new ModelDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setProviderId(entity.getProviderId());
        dto.setModelId(entity.getModelId());
        dto.setName(entity.getName());
        dto.setModelEndpoint(entity.getModelEndpoint());
        dto.setDescription(entity.getDescription());
        dto.setIsOfficial(entity.getOfficial());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static ModelEntity toModelEntity(ModelCreateRequest request) {
        ModelEntity entity = new ModelEntity();
        entity.setId(generateId());
        entity.setProviderId(request.getProviderId());
        entity.setModelId(request.getModelId());
        entity.setName(request.getName());
        entity.setModelEndpoint(request.getModelEndpoint());
        entity.setDescription(request.getDescription());
        entity.setOfficial(request.getIsOfficial());
        entity.setType(request.getType());
        entity.setStatus(true);
        return entity;
    }

    public static void updateModelEntity(ModelEntity entity, ModelUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getModelEndpoint() != null) {
            entity.setModelEndpoint(request.getModelEndpoint());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    private static String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
