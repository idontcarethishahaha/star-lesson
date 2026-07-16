package com.tianji.aigc.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentContextBus {

    private static final String RECOMMENDED_COURSES = "recommendedCourses";
    private static final String CONSULTED_COURSE = "consultedCourse";
    private static final String CURRENT_TASK = "currentTask";
    private static final String CONVERSATION_HISTORY = "conversationHistory";

    private final Map<String, Map<String, Object>> sessionContextMap = new ConcurrentHashMap<>();

    private Map<String, Object> getSessionContext(String sessionId) {
        return sessionContextMap.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
    }

    public void put(String sessionId, String key, Object value) {
        getSessionContext(sessionId).put(key, value);
        log.debug("AgentContextBus put: sessionId={}, key={}, value={}", sessionId, key, value);
    }

    public Object get(String sessionId, String key) {
        return getSessionContext(sessionId).get(key);
    }

    public <T> T get(String sessionId, String key, Class<T> clazz) {
        Object value = get(sessionId, key);
        if (value == null) {
            return null;
        }
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            log.warn("Type cast failed for key={}, expected={}, actual={}", key, clazz.getName(), value.getClass().getName());
            return null;
        }
    }

    public boolean contains(String sessionId, String key) {
        return getSessionContext(sessionId).containsKey(key);
    }

    public void remove(String sessionId, String key) {
        getSessionContext(sessionId).remove(key);
    }

    public void clear(String sessionId) {
        sessionContextMap.remove(sessionId);
        log.debug("AgentContextBus cleared: sessionId={}", sessionId);
    }

    public void setRecommendedCourses(String sessionId, String courses) {
        put(sessionId, RECOMMENDED_COURSES, courses);
    }

    public String getRecommendedCourses(String sessionId) {
        return get(sessionId, RECOMMENDED_COURSES, String.class);
    }

    public void setConsultedCourse(String sessionId, String course) {
        put(sessionId, CONSULTED_COURSE, course);
    }

    public String getConsultedCourse(String sessionId) {
        return get(sessionId, CONSULTED_COURSE, String.class);
    }

    public void setCurrentTask(String sessionId, String task) {
        put(sessionId, CURRENT_TASK, task);
    }

    public String getCurrentTask(String sessionId) {
        return get(sessionId, CURRENT_TASK, String.class);
    }

    public void appendConversationHistory(String sessionId, String message) {
        Map<String, Object> context = getSessionContext(sessionId);
        StringBuilder history = (StringBuilder) context.computeIfAbsent(CONVERSATION_HISTORY, k -> new StringBuilder());
        history.append(message).append("\n");
    }

    public String getConversationHistory(String sessionId) {
        StringBuilder history = get(sessionId, CONVERSATION_HISTORY, StringBuilder.class);
        return history != null ? history.toString() : "";
    }

    public Map<String, Object> getAll(String sessionId) {
        return getSessionContext(sessionId);
    }
}
