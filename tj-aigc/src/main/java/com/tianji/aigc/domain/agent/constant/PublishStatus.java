package com.tianji.aigc.domain.agent.constant;

import com.tianji.aigc.infrastructure.exception.BusinessException;

public enum PublishStatus {

    REVIEWING(1, "审核中"),

    PUBLISHED(2, "已发布"),

    REJECTED(3, "拒绝"),

    REMOVED(4, "已下架");

    private final Integer code;
    private final String description;

    PublishStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PublishStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (PublishStatus status : PublishStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        throw new BusinessException("INVALID_STATUS_CODE", "无效的发布状态码: " + code);
    }
}
