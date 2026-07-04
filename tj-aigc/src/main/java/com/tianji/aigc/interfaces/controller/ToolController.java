package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.tool.dto.*;
import com.tianji.aigc.application.tool.service.ToolAppService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolAppService toolAppService;

    @PostMapping
    public String createTool(@RequestBody ToolCreateRequest request) {
        String userId = getCurrentUserId();
        return toolAppService.createTool(request, userId);
    }

    @PutMapping("/{id}")
    public void updateTool(@PathVariable String id, @RequestBody ToolUpdateRequest request) {
        toolAppService.updateTool(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTool(@PathVariable String id) {
        toolAppService.deleteTool(id);
    }

    @GetMapping("/{id}")
    public ToolDTO getToolById(@PathVariable String id) {
        String userId = getCurrentUserId();
        return toolAppService.getToolById(id, userId);
    }

    @GetMapping
    public PageDTO<ToolDTO> searchTools(ToolSearchRequest request) {
        String userId = getCurrentUserId();
        return toolAppService.searchTools(request, userId);
    }

    @GetMapping("/my")
    public List<ToolDTO> listMyTools() {
        String userId = getCurrentUserId();
        return toolAppService.listMyTools(userId);
    }

    @PostMapping("/install")
    public String installTool(@RequestBody ToolInstallRequest request) {
        String userId = getCurrentUserId();
        return toolAppService.installTool(request, userId);
    }

    @DeleteMapping("/{toolId}/install")
    public void uninstallTool(@PathVariable String toolId) {
        String userId = getCurrentUserId();
        toolAppService.uninstallTool(toolId, userId);
    }

    @GetMapping("/installed")
    public List<ToolDTO> listInstalledTools() {
        String userId = getCurrentUserId();
        return toolAppService.listInstalledTools(userId);
    }

    @PostMapping("/{toolId}/versions")
    public String createVersion(@PathVariable String toolId,
                                @RequestBody ToolVersionCreateRequest request) {
        String userId = getCurrentUserId();
        request.setToolId(toolId);
        return toolAppService.createVersion(request, userId);
    }

    @GetMapping("/{toolId}/versions")
    public List<ToolVersionDTO> listVersions(@PathVariable String toolId) {
        return toolAppService.listVersions(toolId);
    }

    @GetMapping("/{toolId}/versions/{versionId}")
    public ToolVersionDTO getVersion(@PathVariable String toolId,
                                     @PathVariable String versionId) {
        return toolAppService.getVersion(toolId, versionId);
    }

    private String getCurrentUserId() {
        Long userId = UserContext.getUser();
        return userId != null ? String.valueOf(userId) : "1";
    }
}
