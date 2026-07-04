package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CourseBoardVO {
    @Schema(description = "课程总数")
    private Integer courseNum;
    @Schema(description = "已上架课程数")
    private Integer upCourse;
    @Schema(description = "已下架课程数")
    private Integer downCourse;
    @Schema(description = "待上架课程数")
    private Integer waitUpCourse;
    @Schema(description = "已结束课程数")
    private Integer endCourse;
}
