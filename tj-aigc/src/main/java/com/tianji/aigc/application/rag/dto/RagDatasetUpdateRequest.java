package com.tianji.aigc.application.rag.dto;

import lombok.Data;

@Data
public class RagDatasetUpdateRequest {

    private String name;

    private String icon;

    private String description;

    private Boolean isPublic;
}