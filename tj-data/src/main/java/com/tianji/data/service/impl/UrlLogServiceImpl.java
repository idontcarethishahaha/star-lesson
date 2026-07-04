package com.tianji.data.service.impl;

import com.tianji.common.utils.JsonUtils;
import com.tianji.data.constants.RedisConstants;
import com.tianji.data.model.vo.UrlLogPageVO;
import com.tianji.data.model.vo.UrlLogVO;
import com.tianji.data.model.vo.UrlMetricVO;
import com.tianji.data.service.UrlLogService;
import com.tianji.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlLogServiceImpl implements UrlLogService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private List<UrlLogVO> getAllLogs() {
        String key = RedisConstants.KEY_URL_LOG + DataUtils.getVersion(1);
        Object originData = redisTemplate.opsForValue().get(key);
        if (originData == null) {
            return new ArrayList<>();
        }
        return JsonUtils.toList(originData.toString(), UrlLogVO.class);
    }

    @Override
    public UrlLogPageVO getPageByUrl(Integer pageNo, Integer pageSize, String url, String beginTime, String endTime) {
        List<UrlLogVO> logs = getAllLogs().stream()
                .filter(l -> url == null || url.isEmpty() || url.equals(l.getRequestUri()))
                .collect(Collectors.toList());
        long total = logs.size();
        int from = Math.max(0, (pageNo - 1) * pageSize);
        int to = Math.min(logs.size(), from + pageSize);
        List<UrlLogVO> page = (from >= logs.size()) ? new ArrayList<>() : logs.subList(from, to);
        return new UrlLogPageVO(page, total);
    }

    @Override
    public UrlLogPageVO getPageByUrlLike(Integer pageNo, Integer pageSize, String url, String beginTime, String endTime) {
        List<UrlLogVO> logs = getAllLogs().stream()
                .filter(l -> url == null || url.isEmpty() || (l.getRequestUri() != null && l.getRequestUri().contains(url)))
                .collect(Collectors.toList());
        long total = logs.size();
        int from = Math.max(0, (pageNo - 1) * pageSize);
        int to = Math.min(logs.size(), from + pageSize);
        List<UrlLogVO> page = (from >= logs.size()) ? new ArrayList<>() : logs.subList(from, to);
        return new UrlLogPageVO(page, total);
    }

    @Override
    public UrlMetricVO getMetricByUrl(String url, String beginTime, String endTime) {
        List<UrlLogVO> logs = getAllLogs().stream()
                .filter(l -> url == null || url.isEmpty() || url.equals(l.getRequestUri()))
                .collect(Collectors.toList());
        return buildMetric(logs);
    }

    @Override
    public UrlMetricVO getMetricByUrlLike(String url, String beginTime, String endTime) {
        List<UrlLogVO> logs = getAllLogs().stream()
                .filter(l -> url == null || url.isEmpty() || (l.getRequestUri() != null && l.getRequestUri().contains(url)))
                .collect(Collectors.toList());
        return buildMetric(logs);
    }

    private UrlMetricVO buildMetric(List<UrlLogVO> logs) {
        UrlMetricVO metric = new UrlMetricVO();
        metric.setPv((long) logs.size());
        metric.setUv(logs.stream().map(l -> l.getUserId()).distinct().count());
        if (!logs.isEmpty()) {
            double avg = logs.stream().mapToLong(l -> l.getResponseTime() == null ? 0 : l.getResponseTime()).average().orElse(0);
            metric.setAvgResponseTime(avg);
        } else {
            metric.setAvgResponseTime(0.0);
        }
        long errorCount = logs.stream().filter(l -> {
            String code = l.getResponseCode();
            if (code == null) return false;
            try { int c = Integer.parseInt(code.trim()); return c >= 400; } catch (Exception e) { return false; }
        }).count();
        metric.setErrorCount(errorCount);
        return metric;
    }
}
