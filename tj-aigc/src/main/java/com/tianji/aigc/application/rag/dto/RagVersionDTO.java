package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RagVersionDTO {

    private String id;

    private String name;

    private String icon;

    private String description;

    private String version;

    private String changeLog;

    private String originalRagId;

    private Integer fileCount;

    private Long totalSize;

    private Integer documentCount;

    private Integer publishStatus;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;
}