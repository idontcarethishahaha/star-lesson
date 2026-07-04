package com.tianji.aigc.application.llm.dto;

import com.tianji.aigc.domain.llm.model.enums.ModelType;
import lombok.Data;

@Data
public class ModelDTO {

    private String id;

    private String userId;

    private String providerId;

    private String providerName;

    private String modelId;

    private String name;

    private String modelEndpoint;

    private String description;

    private Boolean isOfficial;

    private ModelType type;

    private Boolean status;
}
