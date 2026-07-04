package com.tianji.aigc.application.llm.dto;

import lombok.Data;

@Data
public class ModelUpdateRequest {

    private String name;

    private String modelEndpoint;

    private String description;

    private Boolean status;
}
