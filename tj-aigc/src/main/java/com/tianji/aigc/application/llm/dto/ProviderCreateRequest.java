package com.tianji.aigc.application.llm.dto;

import lombok.Data;

@Data
public class ProviderCreateRequest {

    private String protocol;

    private String name;

    private String description;

    private String config;

    private Boolean isOfficial = false;
}
