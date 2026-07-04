package com.tianji.aigc.domain.tool.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.domain.tool.constant.UploadType;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import com.tianji.aigc.infrastructure.converter.ListStringConverter;
import com.tianji.aigc.infrastructure.converter.ToolDefinitionListConverter;
import com.tianji.aigc.infrastructure.converter.UploadTypeConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.util.List;

@TableName(value = "tool_versions", autoResultMap = true)
public class ToolVersionEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("name")
    private String name;

    @TableField("icon")
    private String icon;

    @TableField("subtitle")
    private String subtitle;

    @TableField("description")
    private String description;

    @TableField("user_id")
    private String userId;

    @TableField("version")
    private String version;

    @TableField("tool_id")
    private String toolId;

    @TableField(value = "upload_type", typeHandler = UploadTypeConverter.class)
    private UploadType uploadType;

    @TableField("upload_url")
    private String uploadUrl;

    @TableField(value = "tool_list", typeHandler = ToolDefinitionListConverter.class)
    private List<ToolDefinition> toolList;

    @TableField(value = "labels", typeHandler = ListStringConverter.class)
    private List<String> labels;

    @TableField("is_office")
    private Boolean isOffice;

    @TableField("public_status")
    private Boolean publicStatus;

    @TableField("change_log")
    private String changeLog;

    @TableField("mcp_server_name")
    private String mcpServerName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public void setUploadType(UploadType uploadType) {
        this.uploadType = uploadType;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public List<ToolDefinition> getToolList() {
        return toolList;
    }

    public void setToolList(List<ToolDefinition> toolList) {
        this.toolList = toolList;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Boolean getIsOffice() {
        return isOffice;
    }

    public void setIsOffice(Boolean isOffice) {
        this.isOffice = isOffice;
    }

    public Boolean getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(Boolean publicStatus) {
        this.publicStatus = publicStatus;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getMcpServerName() {
        return mcpServerName;
    }

    public void setMcpServerName(String mcpServerName) {
        this.mcpServerName = mcpServerName;
    }
}
