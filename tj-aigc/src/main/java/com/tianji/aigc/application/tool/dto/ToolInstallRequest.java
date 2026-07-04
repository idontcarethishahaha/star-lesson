package com.tianji.aigc.application.tool.dto;

import lombok.Data;

@Data
public class ToolInstallRequest {

    private String toolId;

    private String version;

    private Boolean isPublic = false;
}
