package com.tianji.aigc.domain.tool.constant;

public enum ToolStatus {
    WAITING_REVIEW,
    GITHUB_URL_VALIDATE,
    DEPLOYING,
    FETCHING_TOOLS,
    MANUAL_REVIEW,
    APPROVED,
    FAILED,
    PUBLISHED,
    DRAFT,
    OFFLINE;

    public static ToolStatus fromCode(String name) {
        for (ToolStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }
}
