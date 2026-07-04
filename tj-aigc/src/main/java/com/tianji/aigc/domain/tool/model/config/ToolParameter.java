package com.tianji.aigc.domain.tool.model.config;

import java.io.Serializable;
import java.util.Map;

public class ToolParameter implements Serializable {
    private Map<String, ParameterProperty> properties;
    private String[] required;

    public Map<String, ParameterProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, ParameterProperty> properties) {
        this.properties = properties;
    }

    public String[] getRequired() {
        return required;
    }

    public void setRequired(String[] required) {
        this.required = required;
    }
}
