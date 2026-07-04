package com.tianji.aigc.domain.rag.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.infrastructure.converter.ListStringConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@TableName("rag_versions")
public class RagVersionEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String icon;

    private String description;

    private String userId;

    private String version;

    private String changeLog;

    @TableField(value = "labels", typeHandler = ListStringConverter.class)
    private List<String> labels;

    private String originalRagId;

    private String originalRagName;

    private Integer fileCount;

    private Long totalSize;

    private Integer documentCount;

    private Integer publishStatus;

    private String rejectReason;

    private LocalDateTime reviewTime;

    private LocalDateTime publishedAt;

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

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getOriginalRagId() {
        return originalRagId;
    }

    public void setOriginalRagId(String originalRagId) {
        this.originalRagId = originalRagId;
    }

    public String getOriginalRagName() {
        return originalRagName;
    }

    public void setOriginalRagName(String originalRagName) {
        this.originalRagName = originalRagName;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Integer documentCount) {
        this.documentCount = documentCount;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
