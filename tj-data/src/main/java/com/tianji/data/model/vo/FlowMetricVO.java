package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FlowMetricVO {
    @Schema(description = "X轴数据")
    private List<String> xAxis;
    @Schema(description = "系列数据")
    private List<Map<String, Object>> series;
}
