package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AnalysisUserVO {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "头像")
    private String icon;
    @Schema(description = "性别(0男,1女)")
    private Integer sex;
    @Schema(description = "省份")
    private String province;
    @Schema(description = "常访问课程ID列表")
    private List<Long> courseLabels;
    @Schema(description = "课程偏好(0免费,1付费)")
    private Integer freeLabel;
    @Schema(description = "更新时间")
    private String updateTime;
}
