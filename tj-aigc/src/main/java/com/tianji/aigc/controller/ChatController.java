package com.tianji.aigc.controller;

import cn.hutool.json.JSONObject;
import com.tianji.aigc.application.chat.dto.AgentChatDTO;
import com.tianji.aigc.application.chat.service.AgentChatService;
import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.entity.ChatRecord;
import com.tianji.aigc.infrastructure.initializer.PresetAgentInitializer;
import com.tianji.aigc.query.RecordQuery;
import com.tianji.aigc.service.ChatRecordService;
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
    private final ChatRecordService chatRecordService;

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
            List<ChatRecord> chatRecords = chatRecordService.lambdaQuery()
                    .eq(ChatRecord::getConversationId, conversationId)
                    .orderByAsc(ChatRecord::getCreateTime)
                    .list();

            List<RecordItem> records = new ArrayList<>();
            int segmentIndex = 0;
            for (ChatRecord record : chatRecords) {
                try {
                    JSONObject dataJson = new JSONObject(record.getData());
                    String messageType = dataJson.getStr("messageType", "");

                    JSONObject contentJson = new JSONObject();
                    if ("USER".equals(messageType)) {
                        contentJson.set("type", "USER");
                        String textContent = dataJson.getStr("textContent", "");
                        contentJson.set("contents", List.of(Map.of("type", "text", "text", textContent)));
                    } else if ("ASSISTANT".equals(messageType)) {
                        contentJson.set("type", "AI");
                        String textContent = dataJson.getStr("textContent", "");
                        contentJson.set("text", textContent);
                    } else {
                        continue;
                    }

                    RecordItem item = new RecordItem();
                    item.setId(record.getId());
                    item.setSessionId(sessionId);
                    item.setUserId(userId);
                    item.setSegmentIndex(segmentIndex++);
                    item.setContent(contentJson.toString());
                    item.setCreateTime(record.getCreateTime());
                    records.add(item);
                } catch (Exception e) {
                    log.warn("解析聊天记录失败, recordId={}", record.getId(), e);
                }
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
        AgentChatDTO agentChatDTO = AgentChatDTO.builder()
                .question(message)
                .sessionId(sessionId)
                .agentId(PresetAgentInitializer.ROUTE_AGENT_ID)
                .build();
        return this.agentChatService.chat(agentChatDTO);
    }

    @NoWrapper
    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chatGet(@RequestParam String message,
                                     @RequestParam(defaultValue = "") String sessionId) {
        log.info("GET接口调用: message={}, sessionId={}", message, sessionId);
        if (sessionId.isEmpty()) {
            sessionId = null;
        }
        AgentChatDTO agentChatDTO = AgentChatDTO.builder()
                .question(message)
                .sessionId(sessionId)
                .agentId(PresetAgentInitializer.ROUTE_AGENT_ID)
                .build();
        return this.agentChatService.chat(agentChatDTO);
    }

    @NoWrapper
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        String agentId = chatDTO.getAgentId();
        if (agentId == null || agentId.isEmpty()) {
            agentId = PresetAgentInitializer.ROUTE_AGENT_ID;
        }
        AgentChatDTO agentChatDTO = AgentChatDTO.builder()
                .question(chatDTO.getQuestion())
                .sessionId(chatDTO.getSessionId())
                .agentId(agentId)
                .build();
        return this.agentChatService.chat(agentChatDTO);
    }

    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId,
                     @RequestParam(required = false) String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            agentId = PresetAgentInitializer.ROUTE_AGENT_ID;
        }
        this.agentChatService.stop(sessionId);
    }

    @PostMapping("/text")
    public String chatText(@RequestBody String question,
                           @RequestParam(required = false) String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            agentId = PresetAgentInitializer.ROUTE_AGENT_ID;
        }
        return this.agentChatService.chatText(question, agentId);
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
