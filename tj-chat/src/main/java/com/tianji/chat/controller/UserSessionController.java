package com.tianji.chat.controller;

import com.tianji.chat.domain.dto.UserSessionDTO;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.chat.service.IUserSessionService;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
@Slf4j
@Tag(name = "用户会话关联接口")
public class UserSessionController {

    private final IUserSessionService userSessionService;

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
        userSessionService.updateUserSession(id,dto);
    }

    @Operation(summary = "查询用户会话列表")
    @GetMapping("/list")
    public List<UserSession> getUserSessionList() {
        return userSessionService.getUserSessionList();
    }


    @Operation(summary = "根据ID删除用户会话关联")
    @DeleteMapping("/{id}")
    public void deleteUserSession(@PathVariable Long id) {
        userSessionService.deleteUserSession(id);
    }
}