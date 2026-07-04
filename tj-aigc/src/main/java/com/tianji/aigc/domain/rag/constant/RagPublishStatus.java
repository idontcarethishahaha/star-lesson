package com.tianji.aigc.domain.rag.constant;

import com.tianji.aigc.infrastructure.exception.BusinessException;

public enum RagPublishStatus {

    DRAFT(0, "草稿"),

    REVIEWING(1, "审核中"),

    PUBLISHED(2, "已发布"),

    REJECTED(3, "拒绝"),

    REMOVED(4, "已下架");

    private final Integer code;
    private final String description;

    RagPublishStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RagPublishStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (RagPublishStatus status : RagPublishStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        throw new BusinessException("INVALID_RAG_STATUS_CODE", "无效的RAG发布状态码: " + code);
    }
}
