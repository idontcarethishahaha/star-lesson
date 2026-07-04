package com.tianji.aigc.application.tool.dto;

import com.tianji.aigc.domain.tool.constant.ToolStatus;
import com.tianji.aigc.domain.tool.constant.ToolType;
import com.tianji.aigc.domain.tool.constant.UploadType;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ToolDTO {

    private String id;

    private String name;

    private String icon;

    private String subtitle;

    private String description;

    private String userId;

    private List<String> labels;

    private ToolType toolType;

    private UploadType uploadType;

    private String uploadUrl;

    private Map<String, Object> installCommand;

    private List<ToolDefinition> toolList;

    private ToolStatus status;

    private Boolean isOffice;

    private String rejectReason;

    private String mcpServerName;

    private Boolean isGlobal;

    private String version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer versionCount;

    private Boolean installed;

    private String installedVersion;

    private Boolean publicState;

    private String toolId;
}
