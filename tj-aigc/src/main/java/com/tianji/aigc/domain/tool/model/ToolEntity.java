package com.tianji.aigc.domain.tool.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.domain.tool.constant.ToolStatus;
import com.tianji.aigc.domain.tool.constant.ToolType;
import com.tianji.aigc.domain.tool.constant.UploadType;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import com.tianji.aigc.infrastructure.converter.ListStringConverter;
import com.tianji.aigc.infrastructure.converter.MapConverter;
import com.tianji.aigc.infrastructure.converter.ToolDefinitionListConverter;
import com.tianji.aigc.infrastructure.converter.ToolStatusConverter;
import com.tianji.aigc.infrastructure.converter.ToolTypeConverter;
import com.tianji.aigc.infrastructure.converter.UploadTypeConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.util.List;
import java.util.Map;

@TableName(value = "tools", autoResultMap = true)
public class ToolEntity extends BaseEntity {

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

    @TableField(value = "labels", typeHandler = ListStringConverter.class)
    private List<String> labels;

    @TableField(value = "tool_type", typeHandler = ToolTypeConverter.class)
    private ToolType toolType = ToolType.MCP;

    @TableField(value = "upload_type", typeHandler = UploadTypeConverter.class)
    private UploadType uploadType = UploadType.GITHUB;

    @TableField("upload_url")
    private String uploadUrl;

    @TableField(value = "install_command", typeHandler = MapConverter.class)
    private Map<String, Object> installCommand;

    @TableField(value = "tool_list", typeHandler = ToolDefinitionListConverter.class)
    private List<ToolDefinition> toolList;

    @TableField(value = "status", typeHandler = ToolStatusConverter.class)
    private ToolStatus status;

    @TableField("is_office")
    private Boolean isOffice;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField(value = "failed_step_status", typeHandler = ToolStatusConverter.class)
    private ToolStatus failedStepStatus;

    @TableField("mcp_server_name")
    private String mcpServerName;

    @TableField("is_global")
    private Boolean isGlobal;

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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public ToolType getToolType() {
        return toolType;
    }

    public void setToolType(ToolType toolType) {
        this.toolType = toolType;
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

    public Map<String, Object> getInstallCommand() {
        return installCommand;
    }

    public void setInstallCommand(Map<String, Object> installCommand) {
        this.installCommand = installCommand;
    }

    public List<ToolDefinition> getToolList() {
        return toolList;
    }

    public void setToolList(List<ToolDefinition> toolList) {
        this.toolList = toolList;
    }

    public ToolStatus getStatus() {
        return status;
    }

    public void setStatus(ToolStatus status) {
        this.status = status;
    }

    public Boolean getIsOffice() {
        return isOffice;
    }

    public void setIsOffice(Boolean isOffice) {
        this.isOffice = isOffice;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public ToolStatus getFailedStepStatus() {
        return failedStepStatus;
    }

    public void setFailedStepStatus(ToolStatus failedStepStatus) {
        this.failedStepStatus = failedStepStatus;
    }

    public Boolean getOffice() {
        return isOffice;
    }

    public void setOffice(Boolean office) {
        isOffice = office;
    }

    public String getMcpServerName() {
        return mcpServerName;
    }

    public void setMcpServerName(String mcpServerName) {
        this.mcpServerName = mcpServerName;
    }

    public Boolean getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return Boolean.TRUE.equals(this.isGlobal);
    }

    public boolean requiresUserContainer() {
        return !isGlobal();
    }
}
