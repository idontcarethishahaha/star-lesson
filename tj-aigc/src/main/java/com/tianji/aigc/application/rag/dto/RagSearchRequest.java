package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.util.List;

@Data
public class RagSearchRequest {

    private String datasetId;

    private List<String> datasetIds;

    private String query;

    private Double similarityThreshold = 0.6;

    private Integer topK = 6;
}