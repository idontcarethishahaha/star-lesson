package com.tianji.aigc.application.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.rag.assembler.RagAssembler;
import com.tianji.aigc.application.rag.dto.*;
import com.tianji.aigc.domain.rag.constant.RagPublishStatus;
import com.tianji.aigc.domain.rag.model.DocumentUnitEntity;
import com.tianji.aigc.domain.rag.model.FileDetailEntity;
import com.tianji.aigc.domain.rag.model.RagVersionEntity;
import com.tianji.aigc.domain.rag.model.UserRagEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.exception.EntityNotFoundException;
import com.tianji.aigc.mapper.DocumentUnitMapper;
import com.tianji.aigc.mapper.FileDetailMapper;
import com.tianji.aigc.mapper.RagVersionMapper;
import com.tianji.aigc.mapper.UserRagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagAppService {

    private final UserRagMapper userRagMapper;
    private final FileDetailMapper fileDetailMapper;
    private final DocumentUnitMapper documentUnitMapper;
    private final RagVersionMapper ragVersionMapper;
    private final RagVectorService ragVectorService;
    private final RagSearchService ragSearchService;

    @Qualifier("ragVectorizeExecutor")
    private final ThreadPoolTaskExecutor ragVectorizeExecutor;

    @Transactional
    public String createDataset(RagDatasetCreateRequest request, String userId) {
        UserRagEntity entity = RagAssembler.toDatasetEntity(request, userId);
        userRagMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateDataset(String id, RagDatasetUpdateRequest request) {
        UserRagEntity entity = userRagMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("知识库不存在");
        }
        RagAssembler.updateDatasetEntity(entity, request);
        userRagMapper.updateById(entity);
    }

    @Transactional
    public void deleteDataset(String id) {
        UserRagEntity entity = userRagMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("知识库不存在");
        }
        entity.setDeletedAt(LocalDateTime.now());
        userRagMapper.updateById(entity);

        ragVectorService.deleteVectorsByDatasetId(id);

        LambdaQueryWrapper<FileDetailEntity> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(FileDetailEntity::getDataSetId, id);
        List<FileDetailEntity> files = fileDetailMapper.selectList(fileWrapper);
        for (FileDetailEntity file : files) {
            file.setDeletedAt(LocalDateTime.now());
            fileDetailMapper.updateById(file);

            LambdaQueryWrapper<DocumentUnitEntity> docWrapper = new LambdaQueryWrapper<>();
            docWrapper.eq(DocumentUnitEntity::getFileId, file.getId());
            List<DocumentUnitEntity> docs = documentUnitMapper.selectList(docWrapper);
            for (DocumentUnitEntity doc : docs) {
                doc.setDeletedAt(LocalDateTime.now());
                documentUnitMapper.updateById(doc);
            }
        }
    }

    public RagDatasetDTO getDatasetById(String id) {
        UserRagEntity entity = userRagMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("知识库不存在");
        }
        RagDatasetDTO dto = RagAssembler.toDatasetDTO(entity);

        LambdaQueryWrapper<FileDetailEntity> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(FileDetailEntity::getDataSetId, id);
        fileWrapper.isNull(FileDetailEntity::getDeletedAt);
        dto.setFileCount(Math.toIntExact(fileDetailMapper.selectCount(fileWrapper)));

        LambdaQueryWrapper<DocumentUnitEntity> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.isNull(DocumentUnitEntity::getDeletedAt);
        dto.setDocumentCount(Math.toIntExact(documentUnitMapper.selectCount(docWrapper)));

        return dto;
    }

    public PageDTO<RagDatasetDTO> listDatasets(RagDatasetSearchRequest request) {
        Page<UserRagEntity> page = new Page<>(request.getPageNo(), request.getPageSize());
        LambdaQueryWrapper<UserRagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(UserRagEntity::getDeletedAt);
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(UserRagEntity::getName, request.getName());
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserRagEntity::getUserId, request.getUserId());
        }
        if (request.getIsPublic() != null) {
            wrapper.eq(UserRagEntity::getIsPublic, request.getIsPublic());
        }
        wrapper.orderByDesc(UserRagEntity::getCreatedAt);

        Page<UserRagEntity> result = userRagMapper.selectPage(page, wrapper);
        PageDTO<RagDatasetDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(RagAssembler::toDatasetDTO)
                .collect(Collectors.toList()));
        return pageDTO;
    }

    public List<RagDatasetDTO> listAllDatasets(String userId) {
        LambdaQueryWrapper<UserRagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(UserRagEntity::getDeletedAt);
        wrapper.and(w -> w.eq(UserRagEntity::getUserId, userId).or().eq(UserRagEntity::getIsPublic, true));
        wrapper.orderByDesc(UserRagEntity::getCreatedAt);
        return userRagMapper.selectList(wrapper).stream()
                .map(RagAssembler::toDatasetDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String uploadFile(String datasetId, MultipartFile file, String userId) throws IOException {
        UserRagEntity dataset = userRagMapper.selectById(datasetId);
        if (dataset == null) {
            throw new EntityNotFoundException("知识库不存在");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!isValidExtension(ext)) {
            throw new BusinessException("不支持的文件类型: " + ext);
        }

        String filename = generateId() + "." + ext;
        Long size = file.getSize();

        FileDetailEntity fileEntity = new FileDetailEntity();
        fileEntity.setId(generateId());
        fileEntity.setOriginalFilename(originalFilename);
        fileEntity.setFilename(filename);
        fileEntity.setSize(size);
        fileEntity.setExt(ext);
        fileEntity.setContentType(file.getContentType());
        fileEntity.setDataSetId(datasetId);
        fileEntity.setUserId(userId);
        fileEntity.setProcessingStatus(0);

        String content = extractContent(file, ext);
        int totalPages = content.length() > 0 ? 1 : 0;

        if ("pdf".equalsIgnoreCase(ext)) {
            totalPages = estimatePdfPages(size);
        } else if ("md".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext)) {
            totalPages = estimateTextPages(content);
        }

        fileEntity.setFilePageSize(totalPages);
        fileDetailMapper.insert(fileEntity);

        if (content != null && !content.isEmpty()) {
            DocumentUnitEntity docEntity = new DocumentUnitEntity();
            docEntity.setId(generateId());
            docEntity.setFileId(fileEntity.getId());
            docEntity.setPage(1);
            docEntity.setContent(content);
            docEntity.setIsOcr(true);
            documentUnitMapper.insert(docEntity);
        }

        asyncVectorizeFile(fileEntity.getId());

        return fileEntity.getId();
    }

    public void asyncVectorizeFile(String fileId) {
        ragVectorizeExecutor.execute(() -> {
            try {
                ragVectorService.vectorizeFile(fileId);
            } catch (Exception e) {
                log.error("异步向量化文件失败, fileId={}", fileId, e);
            }
        });
    }

    @Transactional
    public void deleteFile(String datasetId, String fileId) {
        FileDetailEntity file = fileDetailMapper.selectById(fileId);
        if (file == null) {
            throw new EntityNotFoundException("文件不存在");
        }
        if (!file.getDataSetId().equals(datasetId)) {
            throw new BusinessException("文件不属于该知识库");
        }

        file.setDeletedAt(LocalDateTime.now());
        fileDetailMapper.updateById(file);

        ragVectorService.deleteVectorsByFileId(fileId);

        LambdaQueryWrapper<DocumentUnitEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentUnitEntity::getFileId, fileId);
        List<DocumentUnitEntity> docs = documentUnitMapper.selectList(wrapper);
        for (DocumentUnitEntity doc : docs) {
            doc.setDeletedAt(LocalDateTime.now());
            documentUnitMapper.updateById(doc);
        }
    }

    public List<FileDetailDTO> listFiles(String datasetId) {
        LambdaQueryWrapper<FileDetailEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileDetailEntity::getDataSetId, datasetId);
        wrapper.isNull(FileDetailEntity::getDeletedAt);
        wrapper.orderByDesc(FileDetailEntity::getCreatedAt);
        return fileDetailMapper.selectList(wrapper).stream()
                .map(RagAssembler::toFileDetailDTO)
                .collect(Collectors.toList());
    }

    public List<DocumentUnitDTO> listDocuments(String fileId) {
        LambdaQueryWrapper<DocumentUnitEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentUnitEntity::getFileId, fileId);
        wrapper.isNull(DocumentUnitEntity::getDeletedAt);
        wrapper.orderByAsc(DocumentUnitEntity::getPage);
        return documentUnitMapper.selectList(wrapper).stream()
                .map(RagAssembler::toDocumentUnitDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createVersion(RagVersionCreateRequest request, String userId) {
        UserRagEntity dataset = userRagMapper.selectById(request.getRagId());
        if (dataset == null) {
            throw new EntityNotFoundException("知识库不存在");
        }

        LambdaQueryWrapper<FileDetailEntity> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(FileDetailEntity::getDataSetId, request.getRagId());
        fileWrapper.isNull(FileDetailEntity::getDeletedAt);
        List<FileDetailEntity> files = fileDetailMapper.selectList(fileWrapper);

        long totalSize = files.stream().mapToLong(f -> f.getSize() != null ? f.getSize() : 0L).sum();
        int fileCount = files.size();

        LambdaQueryWrapper<DocumentUnitEntity> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.isNull(DocumentUnitEntity::getDeletedAt);
        int docCount = Math.toIntExact(documentUnitMapper.selectCount(docWrapper));

        RagVersionEntity version = new RagVersionEntity();
        version.setId(generateId());
        version.setName(dataset.getName());
        version.setIcon(dataset.getIcon());
        version.setDescription(dataset.getDescription());
        version.setUserId(userId);
        version.setVersion(request.getVersion());
        version.setChangeLog(request.getChangeLog());
        version.setOriginalRagId(request.getRagId());
        version.setFileCount(fileCount);
        version.setTotalSize(totalSize);
        version.setDocumentCount(docCount);
        version.setPublishStatus(RagPublishStatus.DRAFT.getCode());

        ragVersionMapper.insert(version);
        return version.getId();
    }

    @Transactional
    public void publishVersion(String versionId) {
        RagVersionEntity version = ragVersionMapper.selectById(versionId);
        if (version == null) {
            throw new EntityNotFoundException("版本不存在");
        }
        version.setPublishStatus(RagPublishStatus.PUBLISHED.getCode());
        version.setPublishedAt(LocalDateTime.now());
        ragVersionMapper.updateById(version);
    }

    public List<RagVersionDTO> listVersions(String ragId) {
        LambdaQueryWrapper<RagVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RagVersionEntity::getOriginalRagId, ragId);
        wrapper.isNull(RagVersionEntity::getDeletedAt);
        wrapper.orderByDesc(RagVersionEntity::getCreatedAt);
        return ragVersionMapper.selectList(wrapper).stream()
                .map(RagAssembler::toVersionDTO)
                .collect(Collectors.toList());
    }

    public void vectorizeFile(String datasetId, String fileId) {
        FileDetailEntity file = fileDetailMapper.selectById(fileId);
        if (file == null) {
            throw new EntityNotFoundException("文件不存在");
        }
        if (!file.getDataSetId().equals(datasetId)) {
            throw new BusinessException("文件不属于该知识库");
        }
        asyncVectorizeFile(fileId);
    }

    public List<RagSearchResultDTO> search(RagSearchRequest request) {
        return ragSearchService.search(request);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private boolean isValidExtension(String ext) {
        return "pdf".equalsIgnoreCase(ext) ||
               "md".equalsIgnoreCase(ext) ||
               "txt".equalsIgnoreCase(ext) ||
               "doc".equalsIgnoreCase(ext) ||
               "docx".equalsIgnoreCase(ext);
    }

    private String extractContent(MultipartFile file, String ext) throws IOException {
        if ("md".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext)) {
            return new String(file.getBytes(), "UTF-8");
        }
        return "";
    }

    private int estimatePdfPages(Long size) {
        if (size == null) return 0;
        return (int) Math.max(1, size / 50000);
    }

    private int estimateTextPages(String content) {
        if (content == null) return 0;
        return (int) Math.max(1, content.length() / 3000);
    }

    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}