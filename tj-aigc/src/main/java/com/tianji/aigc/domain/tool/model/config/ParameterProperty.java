package com.tianji.aigc.domain.tool.model.config;

import lombok.Data;

@Data
public class ParameterProperty {

    private String type;

    private String description;

    private Object defaultValue;

    private Object example;
}
