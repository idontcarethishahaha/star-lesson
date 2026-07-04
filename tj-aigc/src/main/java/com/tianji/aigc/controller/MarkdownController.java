package com.tianji.aigc.controller;

import com.tianji.aigc.domain.po.MarkdownDocs;
import com.tianji.aigc.domain.vo.MarkdownChunkVO;
import com.tianji.aigc.service.IMarkdownDocsService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库接口
 *
 * @author lusy
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Tag(name = "知识库接口")
public class MarkdownController {

    private final IMarkdownDocsService markdownDocsService;

    @Operation(summary = "上传文件到知识库")
    @PostMapping("/upload")
    public MarkdownDocs uploadMarkdown(@RequestParam MultipartFile file,
                                       @RequestParam(defaultValue = "2") Integer level) {
        return markdownDocsService.upload(file, level);
    }

    @Operation(summary = "根据知识库内容对话")
    @GetMapping("/chat")
    public List<MarkdownChunkVO> chatByMarkdownDoc(@RequestParam String message) {
        return markdownDocsService.chatByMarkdownDoc(message);
    }

    @Operation(summary = "分页查询用户知识库文件列表")
    @GetMapping("/page")
    public PageDTO<MarkdownDocs> queryMarkdownPage(PageQuery query) {
        return markdownDocsService.queryMarkdownPage(query);
    }

    @Operation(summary = "根据文件id查看文件内容")
    @GetMapping("/{id}")
    public String getMarkdown(@PathVariable("id") Long fileId) {
        return markdownDocsService.getMarkdown(fileId);
    }

    @Operation(summary = "更新文件内容")
    @PutMapping("/update")
    public void updateMarkdown(@RequestBody MarkdownDocs markdownDocs) {
        markdownDocsService.updateMarkdown(markdownDocs);
    }

    @Operation(summary = "根据文件id删除文件")
    @DeleteMapping("/{id}")
    public void deleteMarkdown(@PathVariable("id") Long fileId) {
        markdownDocsService.removeMarkdown(fileId);
    }
}
