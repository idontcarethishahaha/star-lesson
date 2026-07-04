package com.tianji.aigc.domain.rag.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("user_rags")
public class UserRagEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String ragVersionId;

    private String name;

    private String description;

    private String icon;

    private String version;

    private LocalDateTime installedAt;

    private String originalRagId;

    private Boolean isPublic;

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

    public String getRagVersionId() {
        return ragVersionId;
    }

    public void setRagVersionId(String ragVersionId) {
        this.ragVersionId = ragVersionId;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public String getOriginalRagId() {
        return originalRagId;
    }

    public void setOriginalRagId(String originalRagId) {
        this.originalRagId = originalRagId;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}