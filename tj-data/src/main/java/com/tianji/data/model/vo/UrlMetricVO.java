package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UrlMetricVO {
    @Schema(description = "页面浏览量")
    private Long pv;
    @Schema(description = "独立访客数")
    private Long uv;
    @Schema(description = "平均响应时间(ms)")
    private Double avgResponseTime;
    @Schema(description = "错误数")
    private Long errorCount;
}
