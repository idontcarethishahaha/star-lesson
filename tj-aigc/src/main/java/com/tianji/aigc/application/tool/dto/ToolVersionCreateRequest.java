package com.tianji.aigc.application.tool.dto;

import lombok.Data;

@Data
public class ToolVersionCreateRequest {

    private String toolId;

    private String version;

    private String uploadUrl;

    private String changeLog;

    private String name;

    private String icon;

    private String subtitle;

    private String description;

    private String mcpServerName;
}
