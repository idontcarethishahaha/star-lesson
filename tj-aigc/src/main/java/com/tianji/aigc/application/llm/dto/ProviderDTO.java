package com.tianji.aigc.application.llm.dto;

import lombok.Data;

@Data
public class ProviderDTO {

    private String id;

    private String userId;

    private String protocol;

    private String name;

    private String description;

    private String config;

    private Boolean isOfficial;

    private Boolean status;
}
