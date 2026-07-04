package com.tianji.aigc.application.rag.dto;

import lombok.Data;

@Data
public class RagDatasetSearchRequest {

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String name;

    private String userId;

    private Boolean isPublic;
}