package com.tianji.data.service.impl;

import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.vo.*;
import com.tianji.data.service.AnalysisService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private <T> T getFromRedis(String type, Class<T> clazz) {
        String key = RedisConstants.KEY_ANALYSIS + type + ":" + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        if (originData == null) {
            try {
                T empty = clazz.getDeclaredConstructor().newInstance();
                return empty;
            } catch (Exception e) {
                return null;
            }
        }
        return JsonUtils.toBean(originData.toString(), clazz);
    }

    @Override
    public FlowMetricVO getCourseGenderDuv(String beginTime, String endTime) {
        return getFromRedis("COURSE_GENDER", FlowMetricVO.class);
    }

    @Override
    public FlowMetricVO getCourseProvinceDuv(String beginTime, String endTime) {
        return getFromRedis("COURSE_PROVINCE", FlowMetricVO.class);
    }

    @Override
    public FunnelVO getCourseConversion(String beginTime, String endTime) {
        return getFromRedis("COURSE_CONVERSION", FunnelVO.class);
    }

    @Override
    public AnalysisPageVO<AnalysisCourseVO> getCourseProfile(Integer pageNo, Integer pageSize) {
        String key = RedisConstants.KEY_ANALYSIS + "COURSE_PROFILE:" + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        AnalysisPageVO<AnalysisCourseVO> result = new AnalysisPageVO<>();
        if (originData == null) {
            result.setList(new ArrayList<>());
            result.setTotal(0L);
            return result;
        }
        List<AnalysisCourseVO> list = JsonUtils.toList(originData.toString(), AnalysisCourseVO.class);
        result.setTotal((long) list.size());
        int from = Math.max(0, (pageNo - 1) * pageSize);
        int to = Math.min(list.size(), from + pageSize);
        result.setList(from >= list.size() ? new ArrayList<>() : list.subList(from, to));
        return result;
    }

    @Override
    public AnalysisPageVO<AnalysisUserVO> getUserProfile(Integer pageNo, Integer pageSize) {
        String key = RedisConstants.KEY_ANALYSIS + "USER_PROFILE:" + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        AnalysisPageVO<AnalysisUserVO> result = new AnalysisPageVO<>();
        if (originData == null) {
            result.setList(new ArrayList<>());
            result.setTotal(0L);
            return result;
        }
        List<AnalysisUserVO> list = JsonUtils.toList(originData.toString(), AnalysisUserVO.class);
        result.setTotal((long) list.size());
        int from = Math.max(0, (pageNo - 1) * pageSize);
        int to = Math.min(list.size(), from + pageSize);
        result.setList(from >= list.size() ? new ArrayList<>() : list.subList(from, to));
        return result;
    }
}
