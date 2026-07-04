package com.tianji.data.service;

import com.tianji.data.model.vo.*;

public interface AnalysisService {
    FlowMetricVO getCourseGenderDuv(String beginTime, String endTime);
    FlowMetricVO getCourseProvinceDuv(String beginTime, String endTime);
    FunnelVO getCourseConversion(String beginTime, String endTime);
    AnalysisPageVO<AnalysisCourseVO> getCourseProfile(Integer pageNo, Integer pageSize);
    AnalysisPageVO<AnalysisUserVO> getUserProfile(Integer pageNo, Integer pageSize);
}
