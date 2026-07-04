package com.tianji.aigc.application.tool.dto;

import lombok.Data;

@Data
public class ToolSearchRequest {

    private Integer pageNo = 1;

    private Integer pageSize = 20;

    private String name;

    private String label;

    private String toolType;

    private String userId;

    private Boolean isGlobal;

    private Boolean installed;
}
