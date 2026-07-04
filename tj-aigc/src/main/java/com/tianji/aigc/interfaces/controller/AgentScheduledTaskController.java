package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.agent.dto.AgentScheduledTaskDTO;
import com.tianji.aigc.application.agent.dto.AgentScheduledTaskRequest;
import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.agent.service.AgentScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent-scheduled-tasks")
@RequiredArgsConstructor
public class AgentScheduledTaskController {

    private final AgentScheduledTaskService scheduledTaskService;

    @PostMapping
    public AgentScheduledTaskDTO createTask(@RequestBody AgentScheduledTaskRequest request) {
        return scheduledTaskService.createTask(request);
    }

    @PutMapping("/{id}")
    public AgentScheduledTaskDTO updateTask(@PathVariable String id, @RequestBody AgentScheduledTaskRequest request) {
        return scheduledTaskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        scheduledTaskService.deleteTask(id);
    }

    @GetMapping("/{id}")
    public AgentScheduledTaskDTO getTask(@PathVariable String id) {
        return scheduledTaskService.getTask(id);
    }

    @GetMapping
    public PageDTO<AgentScheduledTaskDTO> listTasks(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean enabled) {
        return scheduledTaskService.listTasks(pageNo, pageSize, taskType, enabled);
    }

    @PostMapping("/{id}/toggle")
    public AgentScheduledTaskDTO toggleTask(@PathVariable String id, @RequestParam boolean enabled) {
        return scheduledTaskService.toggleTask(id, enabled);
    }

    @GetMapping("/preset-templates")
    public List<AgentScheduledTaskDTO> getPresetTemplates() {
        return scheduledTaskService.getPresetTemplates();
    }
}
