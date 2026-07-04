package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.agent.dto.*;
import com.tianji.aigc.application.agent.service.AgentAppService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentAppService agentAppService;

    @PostMapping
    public String createAgent(@RequestBody AgentCreateRequest request) {
        String userId = getCurrentUserId();
        return agentAppService.createAgent(request, userId);
    }

    @PutMapping("/{id}")
    public void updateAgent(@PathVariable String id, @RequestBody AgentUpdateRequest request) {
        agentAppService.updateAgent(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAgent(@PathVariable String id) {
        agentAppService.deleteAgent(id);
    }

    @GetMapping("/{id}")
    public AgentDTO getAgentById(@PathVariable String id) {
        return agentAppService.getAgentById(id);
    }

    @GetMapping
    public PageDTO<AgentDTO> listAgents(AgentSearchRequest request) {
        return agentAppService.listAgents(request);
    }

    @PostMapping("/{agentId}/versions")
    public String createAgentVersion(@PathVariable String agentId, @RequestBody AgentVersionCreateRequest request) {
        String userId = getCurrentUserId();
        return agentAppService.createAgentVersion(agentId, request, userId);
    }

    @PutMapping("/{agentId}/versions/{versionId}")
    public void updateAgentVersion(@PathVariable String agentId,
                                   @PathVariable String versionId,
                                   @RequestBody AgentVersionUpdateRequest request) {
        agentAppService.updateAgentVersion(agentId, versionId, request);
    }

    @DeleteMapping("/{agentId}/versions/{versionId}")
    public void deleteAgentVersion(@PathVariable String agentId, @PathVariable String versionId) {
        agentAppService.deleteAgentVersion(agentId, versionId);
    }

    @GetMapping("/{agentId}/versions/{versionId}")
    public AgentVersionDTO getAgentVersion(@PathVariable String agentId, @PathVariable String versionId) {
        return agentAppService.getAgentVersion(agentId, versionId);
    }

    @GetMapping("/{agentId}/versions")
    public List<AgentVersionDTO> listAgentVersions(@PathVariable String agentId) {
        return agentAppService.listAgentVersions(agentId);
    }

    @PostMapping("/{agentId}/publish")
    public void publishAgentVersion(@PathVariable String agentId, @RequestBody AgentVersionPublishRequest request) {
        agentAppService.publishAgentVersion(agentId, request);
    }

    @PostMapping("/{agentId}/offline")
    public void offlineAgentVersion(@PathVariable String agentId) {
        agentAppService.offlineAgentVersion(agentId);
    }

    @PostMapping("/{agentId}/review/{versionId}")
    public void publishReview(@PathVariable String agentId,
                              @PathVariable String versionId,
                              @RequestParam boolean approved,
                              @RequestParam(required = false) String reason) {
        agentAppService.publishReview(agentId, versionId, approved, reason);
    }

    @PutMapping("/{agentId}/tools")
    public void bindToolsToAgent(@PathVariable String agentId, @RequestBody List<String> toolIds) {
        agentAppService.bindToolsToAgent(agentId, toolIds);
    }

    @PutMapping("/{agentId}/knowledge-bases")
    public void bindKnowledgeBasesToAgent(@PathVariable String agentId, @RequestBody List<String> kbIds) {
        agentAppService.bindKnowledgeBasesToAgent(agentId, kbIds);
    }

    private String getCurrentUserId() {
        Long userId = UserContext.getUser();
        return userId != null ? String.valueOf(userId) : "1";
    }
}
