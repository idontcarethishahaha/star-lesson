package com.tianji.aigc.domain.tool.model.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ToolDefinition {

    private String name;

    private String description;

    private Map<String, ParameterProperty> parameters = new HashMap<>();

    public void addRequiredParameter(String name, String type, String description) {
        ParameterProperty param = new ParameterProperty();
        param.setType(type);
        param.setDescription(description);
        parameters.put(name, param);
    }

    public void addOptionalParameter(String name, String type, String description, Object defaultValue) {
        ParameterProperty param = new ParameterProperty();
        param.setType(type);
        param.setDescription(description);
        param.setDefaultValue(defaultValue);
        parameters.put(name, param);
    }
}
