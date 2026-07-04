package com.tianji.aigc.application.tool.dto;

import lombok.Data;

@Data
public class McpServerCreateRequest {

    private String name;

    private String type;

    private String command;

    private String args;

    private String env;

    private String url;

    private String description;

    private Boolean isGlobal = false;
}
