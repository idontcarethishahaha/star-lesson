package com.tianji.aigc.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@WebFilter(urlPatterns = "/api/v1/chat/*")
public class SensitiveWordFilter extends HttpFilter {

    private static final String SENSITIVE_WORDS_FILE = "sensitive-words.txt";
    private final Set<String> sensitiveWords = new HashSet<>();

    public SensitiveWordFilter() {
        loadSensitiveWords();
    }

    private void loadSensitiveWords() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(SENSITIVE_WORDS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            if (is != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        sensitiveWords.add(line);
                    }
                }
                log.info("敏感词加载完成，共 {} 个", sensitiveWords.size());
            } else {
                log.warn("敏感词文件不存在: {}", SENSITIVE_WORDS_FILE);
            }
        } catch (IOException e) {
            log.warn("加载敏感词文件失败: {}", e.getMessage());
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        String requestBody = getRequestBody(request);
        if (requestBody != null && containsSensitiveWord(requestBody)) {
            log.warn("敏感词检测触发, body={}", requestBody.substring(0, Math.min(100, requestBody.length())));
            response.setStatus(400);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"输入内容包含敏感词\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }

    private String getRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
