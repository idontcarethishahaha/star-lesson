package com.tianji.aigc.application.agent.scheduler;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.application.agent.service.AgentScheduledTaskService;
import com.tianji.aigc.domain.agent.model.AgentScheduledTaskEntity;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentScheduledTaskScheduler {

    private final AgentScheduledTaskService scheduledTaskService;
    private final ChatMemory chatMemory;
    private final ChatSessionMapper chatSessionMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processScheduledTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<AgentScheduledTaskEntity> tasks = scheduledTaskService.getTasksToFire(now);

        if (tasks.isEmpty()) {
            return;
        }

        log.info("发现 {} 个待执行的定时任务", tasks.size());

        for (AgentScheduledTaskEntity task : tasks) {
            try {
                fireTask(task, now);
            } catch (Exception e) {
                log.error("执行定时任务失败, taskId={}, taskName={}", task.getId(), task.getTaskName(), e);
            }
        }
    }

    private void fireTask(AgentScheduledTaskEntity task, LocalDateTime fireTime) {
        log.info("执行定时任务: taskId={}, taskName={}, userId={}", task.getId(), task.getTaskName(), task.getUserId());

        String sessionId = getOrCreateSession(task);
        String conversationId = task.getUserId() + "_" + sessionId;

        String message = buildMessage(task);

        chatMemory.add(conversationId, new AssistantMessage(message));

        scheduledTaskService.updateTaskAfterFire(task.getId(), fireTime);

        log.info("定时任务执行完成: taskId={}, sessionId={}", task.getId(), sessionId);
    }

    private String getOrCreateSession(AgentScheduledTaskEntity task) {
        Long userId = Long.parseLong(task.getUserId());
        String sessionTitle = "定时提醒-" + task.getTaskName();

        ChatSession existing = chatSessionMapper.selectOne(
                Wrappers.<ChatSession>lambdaQuery()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getTitle, sessionTitle)
                        .last("LIMIT 1")
        );

        if (existing != null) {
            existing.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.updateById(existing);
            return existing.getSessionId();
        }

        String sessionId = IdUtil.simpleUUID();
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title(sessionTitle)
                .updateTime(LocalDateTime.now())
                .build();
        chatSessionMapper.insert(chatSession);

        return sessionId;
    }

    private String buildMessage(AgentScheduledTaskEntity task) {
        String template = task.getMessageTemplate();
        if (template == null || template.isEmpty()) {
            template = "该学习了哦！(≧∇≦)ﾉ";
        }
        return "【定时提醒】\n" + template;
    }
}
