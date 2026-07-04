package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlLogPageVO {
    @Schema(description = "日志列表")
    private List<UrlLogVO> list;
    @Schema(description = "总数")
    private Long total;
}
