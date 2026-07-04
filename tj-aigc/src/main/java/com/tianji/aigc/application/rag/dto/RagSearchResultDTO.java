package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagSearchResultDTO {

    private String id;

    private String fileId;

    private Integer page;

    private String content;

    private Double similarityScore;

    private String datasetId;

    private LocalDateTime createdAt;
}