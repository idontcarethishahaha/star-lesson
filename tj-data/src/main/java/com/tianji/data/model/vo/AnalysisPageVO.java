package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AnalysisPageVO<T> {
    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "总数")
    private Long total;
}
