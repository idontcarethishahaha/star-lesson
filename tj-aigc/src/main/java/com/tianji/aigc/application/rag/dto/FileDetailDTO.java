package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDetailDTO {

    private String id;

    private String url;

    private Long size;

    private String filename;

    private String originalFilename;

    private String ext;

    private String contentType;

    private String dataSetId;

    private Integer filePageSize;

    private Integer processingStatus;

    private Integer currentOcrPageNumber;

    private Integer currentEmbeddingPageNumber;

    private Double ocrProcessProgress;

    private Double embeddingProcessProgress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}