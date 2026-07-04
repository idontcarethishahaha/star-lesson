package com.tianji.aigc.domain.rag.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

@TableName("rag_version_files")
public class RagVersionFileEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String ragVersionId;

    private String originalFileId;

    private String fileName;

    private Long fileSize;

    private Integer filePageSize;

    private String fileType;

    private String filePath;

    private Integer processStatus;

    private Integer embeddingStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRagVersionId() {
        return ragVersionId;
    }

    public void setRagVersionId(String ragVersionId) {
        this.ragVersionId = ragVersionId;
    }

    public String getOriginalFileId() {
        return originalFileId;
    }

    public void setOriginalFileId(String originalFileId) {
        this.originalFileId = originalFileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getFilePageSize() {
        return filePageSize;
    }

    public void setFilePageSize(Integer filePageSize) {
        this.filePageSize = filePageSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public Integer getEmbeddingStatus() {
        return embeddingStatus;
    }

    public void setEmbeddingStatus(Integer embeddingStatus) {
        this.embeddingStatus = embeddingStatus;
    }
}
