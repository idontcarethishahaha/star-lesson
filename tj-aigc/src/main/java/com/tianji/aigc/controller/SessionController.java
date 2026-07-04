package com.tianji.aigc.controller;

import com.tianji.aigc.dto.UserSessionDTO;
import com.tianji.aigc.entity.UserSession;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.service.UserSessionService;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
@Slf4j
@Tag(name = "用户会话关联接口")
public class SessionController {

    private final UserSessionService userSessionService;
    private final ChatSessionService chatSessionService;

    @Operation(summary = "创建用户会话关联")
    @PostMapping
    public UserSession createUserSession(@RequestBody UserSessionDTO dto) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登录");
        }
        dto.setUserId(userId);
        return userSessionService.createUserSession(dto);
    }

    @Operation(summary = "修改用户会话关联")
    @PutMapping("/{id}")
    public void updateUserSession(@PathVariable("id") Long id,
                                  UserSessionDTO dto) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登录");
        }
        dto.setUserId(userId);
        userSessionService.updateUserSession(id, dto);
    }

    @Operation(summary = "查询用户会话列表")
    @GetMapping("/list")
    public List<UserSession> getUserSessionList() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            return new ArrayList<>();
        }
        try {
            Map<String, List<ChatSessionVO>> historyMap = chatSessionService.queryHistorySession();
            List<UserSession> result = new ArrayList<>();
            long idCounter = System.currentTimeMillis();
            for (Map.Entry<String, List<ChatSessionVO>> entry : historyMap.entrySet()) {
                for (ChatSessionVO vo : entry.getValue()) {
                    UserSession us = new UserSession();
                    us.setId(idCounter++);
                    us.setUserId(userId);
                    us.setName(vo.getTitle() != null ? vo.getTitle() : "新会话");
                    us.setTag(entry.getKey());
                    us.setSessionId(vo.getSessionId());
                    us.setCreateTime(vo.getUpdateTime() != null ? vo.getUpdateTime() : LocalDateTime.now());
                    result.add(us);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("获取会话列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Operation(summary = "根据ID删除用户会话关联")
    @DeleteMapping("/{id}")
    public void deleteUserSession(@PathVariable Long id) {
        Long userId = UserContext.getUser();
        if (userId == null || id == null) {
            return;
        }
        try {
            UserSession us = userSessionService.getById(id);
            if (us != null && us.getSessionId() != null) {
                chatSessionService.deleteHistorySession(us.getSessionId());
                userSessionService.removeById(id);
            }
        } catch (Exception e) {
            log.warn("删除会话失败: {}", e.getMessage());
        }
    }
}