package com.tianji.aigc.application.tool.dto;

import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import lombok.Data;

import java.util.List;

@Data
public class ToolVersionDTO {

    private String id;

    private String name;

    private String icon;

    private String subtitle;

    private String description;

    private String userId;

    private String version;

    private String toolId;

    private String uploadUrl;

    private List<ToolDefinition> toolList;

    private List<String> labels;

    private Boolean isOffice;

    private Boolean publicStatus;

    private String changeLog;

    private String mcpServerName;

    private String createdAt;
}
