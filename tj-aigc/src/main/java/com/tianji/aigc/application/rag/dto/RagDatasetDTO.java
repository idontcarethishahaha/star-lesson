package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagDatasetDTO {

    private String id;

    private String name;

    private String icon;

    private String description;

    private String userId;

    private Boolean isPublic;

    private Integer fileCount;

    private Integer documentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}