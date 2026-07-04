package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.tool.dto.*;
import com.tianji.aigc.application.tool.service.McpServerAppService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mcp/servers")
@RequiredArgsConstructor
public class McpServerController {

    private final McpServerAppService mcpServerAppService;

    @PostMapping
    public String createServer(@RequestBody McpServerCreateRequest request) {
        String userId = getCurrentUserId();
        return mcpServerAppService.createServer(request, userId);
    }

    @PutMapping("/{id}")
    public void updateServer(@PathVariable String id, @RequestBody McpServerUpdateRequest request) {
        mcpServerAppService.updateServer(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteServer(@PathVariable String id) {
        mcpServerAppService.deleteServer(id);
    }

    @GetMapping("/{id}")
    public McpServerDTO getServerById(@PathVariable String id) {
        return mcpServerAppService.getServerById(id);
    }

    @GetMapping
    public List<McpServerDTO> listServers() {
        String userId = getCurrentUserId();
        return mcpServerAppService.listServers(userId);
    }

    @GetMapping("/my")
    public List<McpServerDTO> listMyServers() {
        String userId = getCurrentUserId();
        return mcpServerAppService.listMyServers(userId);
    }

    @PostMapping("/{id}/start")
    public void startServer(@PathVariable String id) {
        mcpServerAppService.startServer(id);
    }

    @PostMapping("/{id}/stop")
    public void stopServer(@PathVariable String id) {
        mcpServerAppService.stopServer(id);
    }

    private String getCurrentUserId() {
        Long userId = UserContext.getUser();
        return userId != null ? String.valueOf(userId) : "1";
    }
}
