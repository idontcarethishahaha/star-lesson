package com.tianji.data.service;

import com.tianji.data.model.vo.FlowMetricVO;

public interface FlowService {
    FlowMetricVO getDpv(String beginTime, String endTime);
    FlowMetricVO getDau(String beginTime, String endTime);
    FlowMetricVO getDuv(String beginTime, String endTime);
    FlowMetricVO getDnu(String beginTime, String endTime);
    FlowMetricVO getDpvByTime(String beginTime, String endTime);
    FlowMetricVO getDauByProvince(String beginTime, String endTime);
}
