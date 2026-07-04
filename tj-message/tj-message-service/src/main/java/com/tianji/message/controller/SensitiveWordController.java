package com.tianji.message.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.SensitiveWordDTO;
import com.tianji.message.domain.dto.SensitiveWordFormDTO;
import com.tianji.message.domain.query.SensitiveWordPageQuery;
import com.tianji.message.service.ISensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sensitive")
@Tag(name = "敏感词管理接口")
public class SensitiveWordController {

    private final ISensitiveWordService sensitiveWordService;

    @PostMapping
    @Operation(summary = "新增敏感词")
    public void saveSensitiveWord(@Valid @RequestBody SensitiveWordFormDTO sensitiveWordFormDTO) {
        sensitiveWordService.saveSensitiveWord(sensitiveWordFormDTO);
    }

    @PutMapping
    @Operation(summary = "更新敏感词")
    public void updateSensitiveWord(@Valid @RequestBody SensitiveWordFormDTO sensitiveWordFormDTO) {
        sensitiveWordService.updateSensitiveWord(sensitiveWordFormDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询敏感词")
    public PageDTO<SensitiveWordDTO> listSensitiveWords(SensitiveWordPageQuery pageQuery) {
        return sensitiveWordService.listSensitiveWords(pageQuery);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除敏感词")
    public void deleteSensitiveWord(@Parameter(description = "敏感词id", example = "1") @PathVariable("id") Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
    }
}