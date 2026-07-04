package com.tianji.aigc.domain.vo;

import lombok.Data;

@Data
public class MarkdownChunkVO {
    private String title;
    private String content;
    private Double score;

    public static MarkdownChunkVO fromString(String str) {
        String[] parts = str.split("\ncontent:\n", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("输入的字符串格式不正确，无法解析为 MarkdownChunkVO 对象");
        }
        String title = parts[0].substring("title: ".length());
        String content = parts[1];
        MarkdownChunkVO vo = new MarkdownChunkVO();
        vo.setTitle(title);
        vo.setContent(content);
        return vo;
    }
}
