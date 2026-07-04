package com.tianji.data.controller;

import com.tianji.data.model.vo.TodoVO;
import com.tianji.data.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/data/todo")
@Tag(name = "待办数据相关操作")
@Slf4j
public class TodoController {

    @Resource
    private TodoService todoService;

    @GetMapping
    @Operation(summary = "待办数据获取")
    public TodoVO get() {
        return todoService.get();
    }
}
