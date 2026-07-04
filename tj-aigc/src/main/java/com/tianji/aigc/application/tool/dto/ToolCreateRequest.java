package com.tianji.aigc.application.tool.dto;

import com.tianji.aigc.domain.tool.constant.ToolType;
import com.tianji.aigc.domain.tool.constant.UploadType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ToolCreateRequest {

    private String name;

    private String icon;

    private String subtitle;

    private String description;

    private List<String> labels;

    private ToolType toolType = ToolType.MCP;

    private UploadType uploadType = UploadType.GITHUB;

    private String uploadUrl;

    private Map<String, Object> installCommand;

    private Boolean isGlobal = false;
}
