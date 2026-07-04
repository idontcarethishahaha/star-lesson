package com.tianji.aigc.application.rag.service;

import com.tianji.aigc.application.rag.dto.RagSearchRequest;
import com.tianji.aigc.application.rag.dto.RagSearchResultDTO;
import com.tianji.aigc.domain.rag.model.DocumentUnitEntity;
import com.tianji.aigc.domain.rag.model.FileDetailEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.mapper.FileDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagSearchService {

    private final RagVectorService ragVectorService;
    private final FileDetailMapper fileDetailMapper;

    public List<RagSearchResultDTO> search(RagSearchRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new BusinessException("查询内容不能为空");
        }

        List<String> datasetIds = resolveDatasetIds(request);
        if (datasetIds == null || datasetIds.isEmpty()) {
            throw new BusinessException("请指定知识库");
        }

        List<DocumentUnitEntity> results = ragVectorService.search(
                datasetIds,
                request.getQuery(),
                request.getSimilarityThreshold(),
                request.getTopK()
        );

        return results.stream()
                .map(this::toSearchResultDTO)
                .collect(Collectors.toList());
    }

    public List<RagSearchResultDTO> search(List<String> datasetIds, String query,
                                            Double similarityThreshold, Integer topK) {
        if (datasetIds == null || datasetIds.isEmpty()) {
            return new ArrayList<>();
        }
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<DocumentUnitEntity> results = ragVectorService.search(
                datasetIds,
                query,
                similarityThreshold,
                topK
        );

        return results.stream()
                .map(this::toSearchResultDTO)
                .collect(Collectors.toList());
    }

    public String generateContext(RagSearchRequest request) {
        List<RagSearchResultDTO> results = search(request);
        if (results.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是从知识库中检索到的相关内容：\n\n");

        int index = 1;
        for (RagSearchResultDTO result : results) {
            context.append("【资料").append(index).append("】");
            if (result.getFileId() != null) {
                FileDetailEntity file = fileDetailMapper.selectById(result.getFileId());
                if (file != null) {
                    context.append("（文件：").append(file.getOriginalFilename()).append("）");
                }
            }
            context.append("\n");
            context.append(result.getContent()).append("\n\n");
            index++;
        }

        return context.toString();
    }

    private List<String> resolveDatasetIds(RagSearchRequest request) {
        if (request.getDatasetId() != null && !request.getDatasetId().isEmpty()) {
            return List.of(request.getDatasetId());
        }
        if (request.getDatasetIds() != null && !request.getDatasetIds().isEmpty()) {
            return request.getDatasetIds();
        }
        return null;
    }

    private RagSearchResultDTO toSearchResultDTO(DocumentUnitEntity entity) {
        RagSearchResultDTO dto = new RagSearchResultDTO();
        dto.setId(entity.getId());
        dto.setFileId(entity.getFileId());
        dto.setPage(entity.getPage());
        dto.setContent(entity.getContent());
        dto.setSimilarityScore(entity.getSimilarityScore());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}