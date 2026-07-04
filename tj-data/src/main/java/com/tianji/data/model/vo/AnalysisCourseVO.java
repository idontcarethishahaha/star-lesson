package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AnalysisCourseVO {
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "封面URL")
    private String coverUrl;
    @Schema(description = "课程价格(分)")
    private String price;
    @Schema(description = "访问用户性别标签")
    private String sexLabel;
    @Schema(description = "访问用户省份标签列表")
    private List<String> provinceLabels;
    @Schema(description = "更新时间")
    private String updateTime;
}
