package com.tianji.aigc.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SummarizingChatMemory implements ChatMemory {

    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatModel chatModel;
    private final TokenCounter tokenCounter;
    private final int maxTokenLimit;
    private final int summaryThreshold;

    private static final String SUMMARY_KEY = "_summary";
    private static final String SUMMARY_SYSTEM_TEMPLATE = "这是之前对话的摘要总结：\n%s\n\n请基于这个摘要继续对话。";

    public SummarizingChatMemory(ChatMemoryRepository chatMemoryRepository,
                                  ChatModel chatModel,
                                  TokenCounter tokenCounter,
                                  int maxTokenLimit,
                                  int summaryThreshold) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatModel = chatModel;
        this.tokenCounter = tokenCounter;
        this.maxTokenLimit = maxTokenLimit;
        this.summaryThreshold = summaryThreshold;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> allMessages = new ArrayList<>(chatMemoryRepository.findByConversationId(conversationId));
        allMessages.addAll(messages);

        int totalTokens = calculateTotalTokens(allMessages);
        log.debug("对话 {} 总 token 数: {} / 阈值: {}", conversationId, totalTokens, maxTokenLimit);

        if (totalTokens > summaryThreshold) {
            allMessages = summarizeAndCompress(conversationId, allMessages);
        }

        chatMemoryRepository.saveAll(conversationId, allMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messages = new ArrayList<>(chatMemoryRepository.findByConversationId(conversationId));

        String summary = loadSummary(conversationId);
        List<Message> result = new ArrayList<>();

        if (summary != null && !summary.isEmpty()) {
            result.add(new SystemMessage(String.format(SUMMARY_SYSTEM_TEMPLATE, summary)));
        }

        result.addAll(messages);

        return result;
    }

    @Override
    public void clear(String conversationId) {
        chatMemoryRepository.deleteByConversationId(conversationId);
        saveSummary(conversationId, null);
    }

    private List<Message> summarizeAndCompress(String conversationId, List<Message> messages) {
        if (messages.size() < 6) {
            return messages;
        }

        try {
            int midpoint = messages.size() / 2;
            List<Message> toSummarize = messages.subList(0, midpoint);
            List<Message> toKeep = new ArrayList<>(messages.subList(midpoint, messages.size()));

            String existingSummary = loadSummary(conversationId);
            String newSummary = generateSummary(toSummarize, existingSummary);

            saveSummary(conversationId, newSummary);
            log.info("对话 {} 已生成摘要，保留 {} 条近期消息", conversationId, toKeep.size());

            return toKeep;
        } catch (Exception e) {
            log.warn("生成对话摘要失败，使用消息截断策略: {}", e.getMessage());
            int keepCount = Math.min(messages.size(), 20);
            return messages.subList(messages.size() - keepCount, messages.size());
        }
    }

    private String generateSummary(List<Message> messages, String existingSummary) {
        StringBuilder dialogueBuilder = new StringBuilder();
        for (Message msg : messages) {
            if (msg instanceof UserMessage) {
                dialogueBuilder.append("用户: ").append(msg.getText()).append("\n");
            } else if (msg instanceof AssistantMessage) {
                dialogueBuilder.append("助手: ").append(msg.getText()).append("\n");
            }
        }
        String dialogue = dialogueBuilder.toString();

        String prompt;
        if (existingSummary != null && !existingSummary.isEmpty()) {
            prompt = "请基于以下已有的对话摘要和新的对话内容，生成一份更新后的摘要。\n\n" +
                    "【已有摘要】\n" + existingSummary + "\n\n" +
                    "【新增对话内容】\n" + dialogue + "\n\n" +
                    "要求：\n" +
                    "1. 保留重要的用户信息、偏好和需求\n" +
                    "2. 记录已经完成的任务和讨论的话题\n" +
                    "3. 保持简洁，不超过500字\n" +
                    "4. 按主题组织，条理清晰\n\n" +
                    "请直接输出更新后的摘要内容。";
        } else {
            prompt = "请为以下对话生成一份简洁的摘要总结。\n\n" +
                    "【对话内容】\n" + dialogue + "\n\n" +
                    "要求：\n" +
                    "1. 提取用户的核心需求和问题\n" +
                    "2. 记录已经讨论过的话题和达成的共识\n" +
                    "3. 保留用户的重要信息和偏好\n" +
                    "4. 语言简洁，不超过400字\n" +
                    "5. 按要点列出，条理清晰\n\n" +
                    "请直接输出摘要内容。";
        }

        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            throw new RuntimeException("摘要生成失败", e);
        }
    }

    private int calculateTotalTokens(List<Message> messages) {
        int total = 0;
        for (Message message : messages) {
            total += tokenCounter.countTokens(message.getText());
        }
        return total;
    }

    private String loadSummary(String conversationId) {
        try {
            String key = conversationId + SUMMARY_KEY;
            List<Message> summaryMsgs = chatMemoryRepository.findByConversationId(key);
            if (summaryMsgs != null && !summaryMsgs.isEmpty()) {
                return summaryMsgs.get(0).getText();
            }
        } catch (Exception e) {
            log.debug("加载摘要失败: {}", e.getMessage());
        }
        return null;
    }

    private void saveSummary(String conversationId, String summary) {
        try {
            String key = conversationId + SUMMARY_KEY;
            chatMemoryRepository.deleteByConversationId(key);
            if (summary != null && !summary.isEmpty()) {
                chatMemoryRepository.saveAll(key, List.of(new SystemMessage(summary)));
            }
        } catch (Exception e) {
            log.warn("保存摘要失败: {}", e.getMessage());
        }
    }
}
