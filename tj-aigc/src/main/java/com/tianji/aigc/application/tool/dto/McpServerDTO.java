package com.tianji.aigc.application.tool.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class McpServerDTO {

    private String id;

    private String name;

    private String type;

    private String command;

    private String args;

    private String env;

    private String url;

    private String description;

    private String status;

    private String userId;

    private Boolean isGlobal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<String> tools;
}
