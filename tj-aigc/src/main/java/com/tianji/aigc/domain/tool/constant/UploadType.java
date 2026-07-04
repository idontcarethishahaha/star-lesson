package com.tianji.aigc.domain.tool.constant;

import com.tianji.aigc.infrastructure.exception.BusinessException;

public enum UploadType {

    GITHUB, ZIP;

    public static UploadType fromCode(String code) {
        for (UploadType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new BusinessException("未知的上传类型码: " + code);
    }
}
