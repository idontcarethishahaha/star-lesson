package com.tianji.data.controller;

import com.tianji.data.model.vo.UrlLogPageVO;
import com.tianji.data.model.vo.UrlMetricVO;
import com.tianji.data.service.UrlLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data/url")
@Tag(name = "URL日志相关操作")
@Slf4j
public class UrlLogController {

    @Resource
    private UrlLogService urlLogService;

    @GetMapping("/page/log")
    @Operation(summary = "精确匹配URL获取分页日志")
    public UrlLogPageVO getPageByUrl(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return urlLogService.getPageByUrl(pageNo, pageSize, url, beginTime, endTime);
    }

    @GetMapping("/page/log/like")
    @Operation(summary = "模糊匹配URL获取分页日志")
    public UrlLogPageVO getPageByUrlLike(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        return urlLogService.getPageByUrlLike(pageNo, pageSize, url, beginTime, endTime);
    }

    @GetMapping("/metric")
    @Operation(summary = "精确匹配URL获取指标数据")
    public UrlMetricVO getMetricByUrl(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        return urlLogService.getMetricByUrl(url, beginTime, endTime);
    }

    @GetMapping("/metric/like")
    @Operation(summary = "模糊匹配URL获取指标数据")
    public UrlMetricVO getMetricByUrlLike(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        return urlLogService.getMetricByUrlLike(url, beginTime, endTime);
    }

    @GetMapping("/export/logs")
    @Operation(summary = "导出最近7天日志")
    public UrlLogPageVO exportLogs() {
        return urlLogService.getPageByUrl(1, Integer.MAX_VALUE, null, null, null);
    }
}
