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

@TableName(value = "user_tools", autoResultMap = true)
public class UserToolEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;

    @TableField("subtitle")
    private String subtitle;

    @TableField("tool_id")
    private String toolId;

    @TableField("version")
    private String version;

    @TableField(value = "tool_list", typeHandler = ToolDefinitionListConverter.class)
    private List<ToolDefinition> toolList;

    @TableField(value = "labels", typeHandler = ListStringConverter.class)
    private List<String> labels;

    @TableField("is_office")
    private Boolean isOffice;

    @TableField("public_state")
    private Boolean publicState;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Boolean getPublicState() {
        return publicState;
    }

    public void setPublicState(Boolean publicState) {
        this.publicState = publicState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
