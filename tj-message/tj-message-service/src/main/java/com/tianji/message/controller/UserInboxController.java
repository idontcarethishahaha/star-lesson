package com.tianji.message.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.UserInboxDTO;
import com.tianji.message.domain.dto.UserInboxFormDTO;
import com.tianji.message.domain.query.UserInboxQuery;
import com.tianji.message.service.IUserInboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户通知记录 前端控制器
 * </p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/inboxes")
@Tag(name = "用户收件箱接口")
public class UserInboxController {

    private final IUserInboxService inboxService;

    @PostMapping
    @Operation(summary = "发送私信")
    public Long sentMessageToUser(@RequestBody UserInboxFormDTO userInboxFormDTO){
        return inboxService.sentMessageToUser(userInboxFormDTO);
    }

    @Operation(summary = "分页查询收件箱")
    @GetMapping
    public PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query){
        return inboxService.queryUserInBoxesPage(query);
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread")
    public Integer queryUnreadCount(){
        return inboxService.queryUnreadCount();
    }

    @Operation(summary = "根据消息类型获取未读消息数")
    @GetMapping("/unread/{type}")
    public Integer queryUnreadCountByType(@PathVariable("type") Integer type){
        return inboxService.queryUnreadCountByType(type);
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/mark/{id}")
    public void markAsRead(@PathVariable("id") Long id){
        inboxService.markAsRead(id);
    }

    @Operation(summary = "标记全部消息已读")
    @PostMapping("/markAll")
    public void markAllAsRead(){
        inboxService.markAllAsRead();
    }
}