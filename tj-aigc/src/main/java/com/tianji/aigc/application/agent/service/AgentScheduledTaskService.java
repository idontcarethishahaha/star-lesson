package com.tianji.aigc.application.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.aigc.application.agent.dto.AgentScheduledTaskDTO;
import com.tianji.aigc.application.agent.dto.AgentScheduledTaskRequest;
import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.domain.agent.model.AgentScheduledTaskEntity;
import com.tianji.aigc.mapper.AgentScheduledTaskMapper;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentScheduledTaskService {

    private final AgentScheduledTaskMapper scheduledTaskMapper;

    private static final Map<String, String> TASK_TYPE_NAMES = Map.of(
            "STUDY_REMINDER", "学习提醒",
            "DAILY_REVIEW", "每日复习",
            "WEEKLY_REPORT", "周报生成",
            "LEARNING_PLAN", "学习计划提醒",
            "CUSTOM", "自定义"
    );

    private static final List<String> PRESET_TEMPLATES = Arrays.asList(
            "STUDY_REMINDER", "DAILY_REVIEW", "WEEKLY_REPORT", "LEARNING_PLAN"
    );

    @Transactional
    public AgentScheduledTaskDTO createTask(AgentScheduledTaskRequest request) {
        String userId = String.valueOf(UserContext.getUser());

        AgentScheduledTaskEntity entity = new AgentScheduledTaskEntity();
        entity.setUserId(userId);
        entity.setAgentId(request.getAgentId());
        entity.setTaskName(request.getTaskName());
        entity.setTaskType(request.getTaskType() != null ? request.getTaskType() : "CUSTOM");
        entity.setCronExpression(request.getCronExpression());
        entity.setMessageTemplate(generateMessageTemplate(request));
        entity.setTargetCourseId(request.getTargetCourseId());
        entity.setEnabled(true);
        entity.setFireCount(0);
        entity.setRemark(request.getRemark());
        entity.setNextFireTime(calculateNextFireTime(request.getCronExpression()));

        scheduledTaskMapper.insert(entity);
        return toDTO(entity);
    }

    @Transactional
    public AgentScheduledTaskDTO updateTask(String id, AgentScheduledTaskRequest request) {
        String userId = String.valueOf(UserContext.getUser());
        AgentScheduledTaskEntity entity = scheduledTaskMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限");
        }

        if (request.getTaskName() != null) entity.setTaskName(request.getTaskName());
        if (request.getCronExpression() != null) {
            entity.setCronExpression(request.getCronExpression());
            entity.setNextFireTime(calculateNextFireTime(request.getCronExpression()));
        }
        if (request.getMessageTemplate() != null) entity.setMessageTemplate(request.getMessageTemplate());
        if (request.getTargetCourseId() != null) entity.setTargetCourseId(request.getTargetCourseId());
        if (request.getRemark() != null) entity.setRemark(request.getRemark());

        scheduledTaskMapper.updateById(entity);
        return toDTO(entity);
    }

    @Transactional
    public void deleteTask(String id) {
        String userId = String.valueOf(UserContext.getUser());
        AgentScheduledTaskEntity entity = scheduledTaskMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限");
        }
        scheduledTaskMapper.deleteById(id);
    }

    @Transactional
    public AgentScheduledTaskDTO toggleTask(String id, boolean enabled) {
        String userId = String.valueOf(UserContext.getUser());
        AgentScheduledTaskEntity entity = scheduledTaskMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限");
        }
        entity.setEnabled(enabled);
        if (enabled) {
            entity.setNextFireTime(calculateNextFireTime(entity.getCronExpression()));
        }
        scheduledTaskMapper.updateById(entity);
        return toDTO(entity);
    }

    public AgentScheduledTaskDTO getTask(String id) {
        String userId = String.valueOf(UserContext.getUser());
        AgentScheduledTaskEntity entity = scheduledTaskMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限");
        }
        return toDTO(entity);
    }

    public PageDTO<AgentScheduledTaskDTO> listTasks(
            Integer pageNo, Integer pageSize, String taskType, Boolean enabled) {
        String userId = String.valueOf(UserContext.getUser());
        Page<AgentScheduledTaskEntity> page = new Page<>(pageNo != null ? pageNo : 1, pageSize != null ? pageSize : 20);

        LambdaQueryWrapper<AgentScheduledTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentScheduledTaskEntity::getUserId, userId);
        if (taskType != null) wrapper.eq(AgentScheduledTaskEntity::getTaskType, taskType);
        if (enabled != null) wrapper.eq(AgentScheduledTaskEntity::getEnabled, enabled);
        wrapper.orderByDesc(AgentScheduledTaskEntity::getCreatedAt);

        Page<AgentScheduledTaskEntity> result = scheduledTaskMapper.selectPage(page, wrapper);

        PageDTO<AgentScheduledTaskDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));

        return pageDTO;
    }

    public List<AgentScheduledTaskDTO> getPresetTemplates() {
        return PRESET_TEMPLATES.stream().map(type -> {
            AgentScheduledTaskDTO dto = new AgentScheduledTaskDTO();
            dto.setTaskType(type);
            dto.setTaskTypeName(TASK_TYPE_NAMES.getOrDefault(type, type));
            dto.setMessageTemplate(getPresetMessage(type));
            dto.setCronExpression(getPresetCron(type));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<AgentScheduledTaskEntity> getTasksToFire(LocalDateTime now) {
        LambdaQueryWrapper<AgentScheduledTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentScheduledTaskEntity::getEnabled, true)
                .isNotNull(AgentScheduledTaskEntity::getNextFireTime)
                .le(AgentScheduledTaskEntity::getNextFireTime, now);
        return scheduledTaskMapper.selectList(wrapper);
    }

    @Transactional
    public void updateTaskAfterFire(String taskId, LocalDateTime fireTime) {
        AgentScheduledTaskEntity entity = scheduledTaskMapper.selectById(taskId);
        if (entity != null) {
            entity.setLastFireTime(fireTime);
            entity.setNextFireTime(calculateNextFireTime(entity.getCronExpression()));
            entity.setFireCount(entity.getFireCount() != null ? entity.getFireCount() + 1 : 1);
            scheduledTaskMapper.updateById(entity);
        }
    }

    private String generateMessageTemplate(AgentScheduledTaskRequest request) {
        if (request.getMessageTemplate() != null && !request.getMessageTemplate().isEmpty()) {
            return request.getMessageTemplate();
        }
        return getPresetMessage(request.getTaskType());
    }

    private String getPresetMessage(String taskType) {
        if (taskType == null) return "该学习了哦！(≧∇≦)ﾉ";
        return switch (taskType) {
            case "STUDY_REMINDER" ->
                    "⏰ 学习时间到啦！\n\n" +
                    "今天也是学习的好日子呢 (≧∇≦)ﾉ\n" +
                    "快来继续你的学习之旅吧！\n" +
                    "哪怕只学15分钟，也是进步哦~";
            case "DAILY_REVIEW" ->
                    "📝 每日复习时间到！\n\n" +
                    "今天学习了什么呢？来复习一下吧~\n" +
                    "温故而知新，坚持就是胜利！";
            case "WEEKLY_REPORT" ->
                    "📊 周末学习总结\n\n" +
                    "又过了一周啦，来看看你的学习成果吧！\n" +
                    "坚持学习的你超棒的！继续加油~";
            case "LEARNING_PLAN" ->
                    "📅 今日学习计划提醒\n\n" +
                    "今天的学习目标完成了吗？\n" +
                    "按照计划学习，效率更高哦~";
            default -> "该学习了哦！(≧∇≦)ﾉ";
        };
    }

    private String getPresetCron(String taskType) {
        if (taskType == null) return "0 0 20 * * ?";
        return switch (taskType) {
            case "STUDY_REMINDER" -> "0 0 20 * * ?";
            case "DAILY_REVIEW" -> "0 30 21 * * ?";
            case "WEEKLY_REPORT" -> "0 0 20 ? * SUN";
            case "LEARNING_PLAN" -> "0 0 8 * * ?";
            default -> "0 0 20 * * ?";
        };
    }

    private LocalDateTime calculateNextFireTime(String cronExpression) {
        if (cronExpression == null || cronExpression.isEmpty()) return null;
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            return cron.next(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("解析 cron 表达式失败: {}", cronExpression);
            return null;
        }
    }

    private AgentScheduledTaskDTO toDTO(AgentScheduledTaskEntity entity) {
        AgentScheduledTaskDTO dto = new AgentScheduledTaskDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setAgentId(entity.getAgentId());
        dto.setTaskName(entity.getTaskName());
        dto.setTaskType(entity.getTaskType());
        dto.setTaskTypeName(TASK_TYPE_NAMES.getOrDefault(entity.getTaskType(), entity.getTaskType()));
        dto.setCronExpression(entity.getCronExpression());
        dto.setMessageTemplate(entity.getMessageTemplate());
        dto.setTargetCourseId(entity.getTargetCourseId());
        dto.setEnabled(entity.getEnabled());
        dto.setLastFireTime(entity.getLastFireTime());
        dto.setNextFireTime(entity.getNextFireTime());
        dto.setFireCount(entity.getFireCount());
        dto.setRemark(entity.getRemark());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
