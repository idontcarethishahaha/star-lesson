package com.tianji.aigc.application.llm.dto;

import lombok.Data;

@Data
public class ProviderSearchRequest {

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String name;

    private String protocol;

    private Boolean isOfficial;
}
