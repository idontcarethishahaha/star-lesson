package com.tianji.aigc.application.llm.dto;

import lombok.Data;

@Data
public class ProviderUpdateRequest {

    private String name;

    private String description;

    private String config;

    private Boolean status;
}
