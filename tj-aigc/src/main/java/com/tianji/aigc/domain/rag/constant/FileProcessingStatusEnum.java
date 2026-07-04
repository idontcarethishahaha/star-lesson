package com.tianji.aigc.domain.rag.constant;

public enum FileProcessingStatusEnum {

    UPLOADED(0, "已上传"),

    OCR_PROCESSING(1, "OCR处理中"),

    OCR_COMPLETED(2, "OCR处理完成"),

    EMBEDDING_PROCESSING(3, "向量化处理中"),

    COMPLETED(4, "处理完成"),

    OCR_FAILED(5, "OCR处理失败"),

    EMBEDDING_FAILED(6, "向量化处理失败");

    private final Integer code;
    private final String description;

    FileProcessingStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static FileProcessingStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileProcessingStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的文件处理状态码: " + code);
    }

    public boolean isProcessing() {
        return this == OCR_PROCESSING || this == EMBEDDING_PROCESSING;
    }

    public boolean isFailed() {
        return this == OCR_FAILED || this == EMBEDDING_FAILED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean canStartOcr() {
        return this == UPLOADED;
    }

    public boolean canStartEmbedding() {
        return this == OCR_COMPLETED;
    }
}
