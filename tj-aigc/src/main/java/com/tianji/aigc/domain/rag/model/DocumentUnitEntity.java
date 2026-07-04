package com.tianji.aigc.domain.rag.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.aigc.infrastructure.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

@TableName("document_unit")
public class DocumentUnitEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7001509997040094844L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String fileId;

    private Integer page;

    private String content;

    private Boolean isVector;

    private Boolean isOcr;

    private String vectorId;

    @TableField(exist = false)
    private Double similarityScore;

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Boolean getIsOcr() {
        return isOcr;
    }

    public void setIsOcr(Boolean isOcr) {
        this.isOcr = isOcr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsVector() {
        return isVector;
    }

    public void setIsVector(Boolean isVector) {
        this.isVector = isVector;
    }

    public String getVectorId() {
        return vectorId;
    }

    public void setVectorId(String vectorId) {
        this.vectorId = vectorId;
    }
}