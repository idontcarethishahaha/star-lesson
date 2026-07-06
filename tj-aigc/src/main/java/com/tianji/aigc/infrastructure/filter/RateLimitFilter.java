package com.tianji.aigc.infrastructure.filter;

import com.tianji.common.utils.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@WebFilter(urlPatterns = "/api/v1/chat/*")
public class RateLimitFilter extends HttpFilter {

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final int DEFAULT_MAX_REQUESTS = 20;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        Long userId = UserContext.getUser();
        String key = RATE_LIMIT_PREFIX + (userId != null ? userId : request.getRemoteAddr());
        
        long currentTime = Instant.now().getEpochSecond();
        long windowStart = currentTime - DEFAULT_WINDOW_SECONDS;
        
        String windowKey = key + ":window";
        
        stringRedisTemplate.opsForZSet().removeRange(windowKey, 0, windowStart - 1);
        
        Long count = stringRedisTemplate.opsForZSet().size(windowKey);
        if (count != null && count >= DEFAULT_MAX_REQUESTS) {
            log.warn("限流触发, key={}, count={}", key, count);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }
        
        stringRedisTemplate.opsForZSet().add(windowKey, String.valueOf(currentTime), currentTime);
        stringRedisTemplate.expire(windowKey, DEFAULT_WINDOW_SECONDS * 2, TimeUnit.SECONDS);
        
        chain.doFilter(request, response);
    }
}
