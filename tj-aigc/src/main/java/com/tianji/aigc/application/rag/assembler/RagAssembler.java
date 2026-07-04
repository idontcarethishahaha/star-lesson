package com.tianji.aigc.application.rag.assembler;

import com.tianji.aigc.application.rag.dto.*;
import com.tianji.aigc.domain.rag.model.FileDetailEntity;
import com.tianji.aigc.domain.rag.model.DocumentUnitEntity;
import com.tianji.aigc.domain.rag.model.RagVersionEntity;

public class RagAssembler {

    public static RagDatasetDTO toDatasetDTO(com.tianji.aigc.domain.rag.model.UserRagEntity entity) {
        if (entity == null) {
            return null;
        }
        RagDatasetDTO dto = new RagDatasetDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setDescription(entity.getDescription());
        dto.setUserId(entity.getUserId());
        dto.setIsPublic(entity.getIsPublic());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static com.tianji.aigc.domain.rag.model.UserRagEntity toDatasetEntity(RagDatasetCreateRequest request, String userId) {
        com.tianji.aigc.domain.rag.model.UserRagEntity entity = new com.tianji.aigc.domain.rag.model.UserRagEntity();
        entity.setId(generateId());
        entity.setName(request.getName());
        entity.setIcon(request.getIcon());
        entity.setDescription(request.getDescription());
        entity.setUserId(userId);
        entity.setIsPublic(request.getIsPublic());
        return entity;
    }

    public static void updateDatasetEntity(com.tianji.aigc.domain.rag.model.UserRagEntity entity, RagDatasetUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            entity.setIsPublic(request.getIsPublic());
        }
    }

    public static FileDetailDTO toFileDetailDTO(FileDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        FileDetailDTO dto = new FileDetailDTO();
        dto.setId(entity.getId());
        dto.setUrl(entity.getUrl());
        dto.setSize(entity.getSize());
        dto.setFilename(entity.getFilename());
        dto.setOriginalFilename(entity.getOriginalFilename());
        dto.setExt(entity.getExt());
        dto.setContentType(entity.getContentType());
        dto.setDataSetId(entity.getDataSetId());
        dto.setFilePageSize(entity.getFilePageSize());
        dto.setProcessingStatus(entity.getProcessingStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static DocumentUnitDTO toDocumentUnitDTO(DocumentUnitEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentUnitDTO dto = new DocumentUnitDTO();
        dto.setId(entity.getId());
        dto.setFileId(entity.getFileId());
        dto.setPage(entity.getPage());
        dto.setContent(entity.getContent());
        dto.setIsVector(entity.getIsVector());
        dto.setIsOcr(entity.getIsOcr());
        dto.setVectorId(entity.getVectorId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public static RagVersionDTO toVersionDTO(RagVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        RagVersionDTO dto = new RagVersionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setChangeLog(entity.getChangeLog());
        dto.setOriginalRagId(entity.getOriginalRagId());
        dto.setFileCount(entity.getFileCount());
        dto.setTotalSize(entity.getTotalSize());
        dto.setDocumentCount(entity.getDocumentCount());
        dto.setPublishStatus(entity.getPublishStatus());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private static String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}