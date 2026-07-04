package com.tianji.aigc.domain.rag.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import com.tianji.aigc.infrastructure.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("file_detail")
public class FileDetailEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1055107743652307804L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String url;

    private Long size;

    private String filename;

    private String originalFilename;

    private String basePath;

    private String path;

    private String ext;

    private String contentType;

    private String platform;

    private String thUrl;

    private String thFilename;

    private Long thSize;

    private String thContentType;

    private String objectId;

    private String objectType;

    private String metadata;

    private String userMetadata;

    private String thMetadata;

    private String thUserMetadata;

    private String attr;

    private String fileAcl;

    private String thFileAcl;

    private String hashInfo;

    private String uploadId;

    private Integer uploadStatus;

    private String userId;

    private String dataSetId;

    private Integer filePageSize;

    private Integer processingStatus;

    private Integer currentOcrPageNumber;

    private Integer currentEmbeddingPageNumber;

    private Double ocrProcessProgress;

    private Double embeddingProcessProgress;

    @TableField(exist = false)
    private Object multipartFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getThUrl() {
        return thUrl;
    }

    public void setThUrl(String thUrl) {
        this.thUrl = thUrl;
    }

    public String getThFilename() {
        return thFilename;
    }

    public void setThFilename(String thFilename) {
        this.thFilename = thFilename;
    }

    public Long getThSize() {
        return thSize;
    }

    public void setThSize(Long thSize) {
        this.thSize = thSize;
    }

    public String getThContentType() {
        return thContentType;
    }

    public void setThContentType(String thContentType) {
        this.thContentType = thContentType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getThMetadata() {
        return thMetadata;
    }

    public void setThMetadata(String thMetadata) {
        this.thMetadata = thMetadata;
    }

    public String getThUserMetadata() {
        return thUserMetadata;
    }

    public void setThUserMetadata(String thUserMetadata) {
        this.thUserMetadata = thUserMetadata;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getFileAcl() {
        return fileAcl;
    }

    public void setFileAcl(String fileAcl) {
        this.fileAcl = fileAcl;
    }

    public String getThFileAcl() {
        return thFileAcl;
    }

    public void setThFileAcl(String thFileAcl) {
        this.thFileAcl = thFileAcl;
    }

    public String getHashInfo() {
        return hashInfo;
    }

    public void setHashInfo(String hashInfo) {
        this.hashInfo = hashInfo;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Integer getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(Integer uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public Integer getFilePageSize() {
        return filePageSize;
    }

    public void setFilePageSize(Integer filePageSize) {
        this.filePageSize = filePageSize;
    }

    public Integer getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(Integer processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getCurrentOcrPageNumber() {
        return currentOcrPageNumber;
    }

    public void setCurrentOcrPageNumber(Integer currentOcrPageNumber) {
        this.currentOcrPageNumber = currentOcrPageNumber;
    }

    public Integer getCurrentEmbeddingPageNumber() {
        return currentEmbeddingPageNumber;
    }

    public void setCurrentEmbeddingPageNumber(Integer currentEmbeddingPageNumber) {
        this.currentEmbeddingPageNumber = currentEmbeddingPageNumber;
    }

    public Double getOcrProcessProgress() {
        return ocrProcessProgress;
    }

    public void setOcrProcessProgress(Double ocrProcessProgress) {
        this.ocrProcessProgress = ocrProcessProgress;
    }

    public Double getEmbeddingProcessProgress() {
        return embeddingProcessProgress;
    }

    public void setEmbeddingProcessProgress(Double embeddingProcessProgress) {
        this.embeddingProcessProgress = embeddingProcessProgress;
    }

    public Object getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(Object multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileDetailEntity that = (FileDetailEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
