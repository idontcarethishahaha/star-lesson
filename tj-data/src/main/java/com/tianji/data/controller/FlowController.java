package com.tianji.data.controller;

import com.tianji.data.model.vo.FlowMetricVO;
import com.tianji.data.service.FlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data/flow")
@Tag(name = "流量统计相关操作")
@Slf4j
public class FlowController {

    @Resource
    private FlowService flowService;

    @GetMapping("/dpv")
    @Operation(summary = "获取DPV(页面浏览量)数据")
    public FlowMetricVO getDpv(@RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime) {
        return flowService.getDpv(beginTime, endTime);
    }

    @GetMapping("/dau")
    @Operation(summary = "获取DAU(活跃用户数)数据")
    public FlowMetricVO getDau(@RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime) {
        return flowService.getDau(beginTime, endTime);
    }

    @GetMapping("/duv")
    @Operation(summary = "获取DUV(独立访客数)数据")
    public FlowMetricVO getDuv(@RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime) {
        return flowService.getDuv(beginTime, endTime);
    }

    @GetMapping("/dnu")
    @Operation(summary = "获取DNU(新增用户数)数据")
    public FlowMetricVO getDnu(@RequestParam(required = false) String beginTime,
                               @RequestParam(required = false) String endTime) {
        return flowService.getDnu(beginTime, endTime);
    }

    @GetMapping("/dpv/time")
    @Operation(summary = "获取DPV时段数据")
    public FlowMetricVO getDpvByTime(@RequestParam(required = false) String beginTime,
                                     @RequestParam(required = false) String endTime) {
        return flowService.getDpvByTime(beginTime, endTime);
    }

    @GetMapping("/dau/province")
    @Operation(summary = "获取DAU省份分布数据")
    public FlowMetricVO getDauByProvince(@RequestParam(required = false) String beginTime,
                                         @RequestParam(required = false) String endTime) {
        return flowService.getDauByProvince(beginTime, endTime);
    }
}
