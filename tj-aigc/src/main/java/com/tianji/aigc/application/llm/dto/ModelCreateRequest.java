package com.tianji.aigc.application.llm.dto;

import com.tianji.aigc.domain.llm.model.enums.ModelType;
import lombok.Data;

@Data
public class ModelCreateRequest {

    private String providerId;

    private String modelId;

    private String name;

    private String modelEndpoint;

    private String description;

    private Boolean isOfficial = false;

    private ModelType type;
}
