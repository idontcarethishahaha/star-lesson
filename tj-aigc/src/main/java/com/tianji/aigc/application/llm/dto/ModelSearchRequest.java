package com.tianji.aigc.application.llm.dto;

import com.tianji.aigc.domain.llm.model.enums.ModelType;
import lombok.Data;

@Data
public class ModelSearchRequest {

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String providerId;

    private String name;

    private ModelType type;

    private Boolean isOfficial;
}
