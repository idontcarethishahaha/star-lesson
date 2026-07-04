package com.tianji.aigc.controller;

import cn.hutool.json.JSONObject;
import com.tianji.aigc.application.chat.dto.AgentChatDTO;
import com.tianji.aigc.application.chat.service.AgentChatService;
import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.query.RecordQuery;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.TemplateVO;
import com.tianji.common.annotations.NoWrapper;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AgentChatService agentChatService;
    private final ChatSessionService chatSessionService;
    private final ChatMemory chatMemory;

    private static final TemplateVO TEMPLATE_VO = new TemplateVO();

    @GetMapping("/records")
    public PageDTO<RecordItem> getRecords(RecordQuery query) {
        String sessionId = query.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            return PageDTO.of(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        }
        Long userId = UserContext.getUser();
        if (userId == null) {
            return PageDTO.of(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        }
        try {
            String conversationId = userId + "_" + sessionId;
            List<Message> messages = chatMemory.get(conversationId);
            List<RecordItem> records = new ArrayList<>();
            int index = 0;
            for (Message msg : messages) {
                String type = msg.getMessageType().name();
                JSONObject contentJson = new JSONObject();
                if ("USER".equals(type)) {
                    contentJson.set("type", "USER");
                    contentJson.set("contents", List.of(Map.of("type", "text", "text", msg.getText())));
                } else {
                    contentJson.set("type", "AI");
                    contentJson.set("text", msg.getText());
                }
                RecordItem item = new RecordItem();
                item.setId((long) index);
                item.setSessionId(sessionId);
                item.setUserId(userId);
                item.setSegmentIndex(index++);
                item.setContent(contentJson.toString());
                item.setCreateTime(LocalDateTime.now());
                records.add(item);
            }
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<RecordItem> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, Math.max(records.size(), 1));
            page.setRecords(records);
            page.setTotal(records.size());
            return PageDTO.of(page);
        } catch (Exception e) {
            log.warn("获取聊天记录失败: {}", e.getMessage());
            return PageDTO.of(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        }
    }

    @NoWrapper
    @GetMapping(value = "/file", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chatFile(@RequestParam String message,
                                      @RequestParam(defaultValue = "") String sessionId) {
        log.info("知识库聊天调用: message={}, sessionId={}", message, sessionId);
        if (sessionId.isEmpty()) {
            sessionId = null;
        }
        return this.chatService.chat(message, sessionId);
    }

    @NoWrapper
    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chatGet(@RequestParam String message,
                                     @RequestParam(defaultValue = "") String sessionId) {
        log.info("GET接口调用: message={}, sessionId={}", message, sessionId);
        if (sessionId.isEmpty()) {
            sessionId = null;
        }
        return this.chatService.chat(message, sessionId);
    }

    @NoWrapper
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        if (chatDTO.getAgentId() != null && !chatDTO.getAgentId().isEmpty()) {
            AgentChatDTO agentChatDTO = AgentChatDTO.builder()
                    .question(chatDTO.getQuestion())
                    .sessionId(chatDTO.getSessionId())
                    .agentId(chatDTO.getAgentId())
                    .build();
            return this.agentChatService.chat(agentChatDTO);
        }
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId,
                     @RequestParam(required = false) String agentId) {
        if (agentId != null && !agentId.isEmpty()) {
            this.agentChatService.stop(sessionId);
        } else {
            this.chatService.stop(sessionId);
        }
    }

    @PostMapping("/text")
    public String chatText(@RequestBody String question,
                           @RequestParam(required = false) String agentId) {
        if (agentId != null && !agentId.isEmpty()) {
            return this.agentChatService.chatText(question, agentId);
        }
        return this.chatService.chatText(question);
    }

    @GetMapping("/templates")
    public TemplateVO getTemplates() {
        return TEMPLATE_VO;
    }

    @GetMapping("/welcome")
    public String getWelcomeMessage(@RequestParam(required = false) String agentId) {
        return this.agentChatService.getWelcomeMessage(agentId);
    }

    @Data
    public static class RecordItem {
        private Long id;
        private Long userId;
        private String sessionId;
        private Integer segmentIndex;
        private String content;
        private LocalDateTime createTime;
    }
}
