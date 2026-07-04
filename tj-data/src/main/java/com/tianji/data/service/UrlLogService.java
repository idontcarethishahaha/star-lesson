package com.tianji.data.service;

import com.tianji.data.model.vo.UrlLogPageVO;
import com.tianji.data.model.vo.UrlMetricVO;

public interface UrlLogService {
    /**
     * 精确匹配 URL 的分页日志
     */
    UrlLogPageVO getPageByUrl(Integer pageNo, Integer pageSize, String url, String beginTime, String endTime);

    /**
     * 模糊匹配 URL 的分页日志
     */
    UrlLogPageVO getPageByUrlLike(Integer pageNo, Integer pageSize, String url, String beginTime, String endTime);

    /**
     * 精确匹配 URL 的指标数据
     */
    UrlMetricVO getMetricByUrl(String url, String beginTime, String endTime);

    /**
     * 模糊匹配 URL 的指标数据
     */
    UrlMetricVO getMetricByUrlLike(String url, String beginTime, String endTime);
}
