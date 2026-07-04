package com.tianji.data.controller;

import com.tianji.data.model.vo.*;
import com.tianji.data.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data/analysis")
@Tag(name = "数据分析相关操作")
@Slf4j
public class AnalysisController {

    @Resource
    private AnalysisService analysisService;

    @GetMapping("/course/conversion")
    @Operation(summary = "获取课程转换漏斗数据")
    public FunnelVO getCourseConversion(@RequestParam(required = false) String beginTime,
                                        @RequestParam(required = false) String endTime) {
        return analysisService.getCourseConversion(beginTime, endTime);
    }

    @GetMapping("/course/gender")
    @Operation(summary = "获取课程访问量性别分布")
    public FlowMetricVO getCourseGenderDuv(@RequestParam(required = false) String beginTime,
                                           @RequestParam(required = false) String endTime) {
        return analysisService.getCourseGenderDuv(beginTime, endTime);
    }

    @GetMapping("/course/province")
    @Operation(summary = "获取课程访问量省份排名")
    public FlowMetricVO getCourseProvinceDuv(@RequestParam(required = false) String beginTime,
                                             @RequestParam(required = false) String endTime) {
        return analysisService.getCourseProvinceDuv(beginTime, endTime);
    }

    @GetMapping("/course/profile")
    @Operation(summary = "获取课程画像分页数据")
    public AnalysisPageVO<AnalysisCourseVO> getCourseProfile(@RequestParam(required = false) Integer pageNo,
                                                             @RequestParam(required = false) Integer pageSize) {
        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return analysisService.getCourseProfile(pageNo, pageSize);
    }

    @GetMapping("/user/profile")
    @Operation(summary = "获取用户画像分页数据")
    public AnalysisPageVO<AnalysisUserVO> getUserProfile(@RequestParam(required = false) Integer pageNo,
                                                         @RequestParam(required = false) Integer pageSize) {
        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return analysisService.getUserProfile(pageNo, pageSize);
    }
}
