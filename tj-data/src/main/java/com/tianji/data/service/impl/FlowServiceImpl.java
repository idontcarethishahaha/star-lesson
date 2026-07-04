package com.tianji.data.service.impl;

import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.vo.FlowMetricVO;
import com.tianji.data.service.FlowService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlowServiceImpl implements FlowService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private FlowMetricVO getMetric(String type, String beginTime, String endTime) {
        String key = RedisConstants.KEY_FLOW + type + ":" + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        if (originData == null) {
            FlowMetricVO empty = new FlowMetricVO();
            empty.setXAxis(new ArrayList<>());
            empty.setSeries(new ArrayList<>());
            return empty;
        }
        return JsonUtils.toBean(originData.toString(), FlowMetricVO.class);
    }

    @Override
    public FlowMetricVO getDpv(String beginTime, String endTime) {
        return getMetric("DPV", beginTime, endTime);
    }

    @Override
    public FlowMetricVO getDau(String beginTime, String endTime) {
        return getMetric("DAU", beginTime, endTime);
    }

    @Override
    public FlowMetricVO getDuv(String beginTime, String endTime) {
        return getMetric("DUV", beginTime, endTime);
    }

    @Override
    public FlowMetricVO getDnu(String beginTime, String endTime) {
        return getMetric("DNU", beginTime, endTime);
    }

    @Override
    public FlowMetricVO getDpvByTime(String beginTime, String endTime) {
        return getMetric("DPV_TIME", beginTime, endTime);
    }

    @Override
    public FlowMetricVO getDauByProvince(String beginTime, String endTime) {
        return getMetric("DAU_PROVINCE", beginTime, endTime);
    }
}
