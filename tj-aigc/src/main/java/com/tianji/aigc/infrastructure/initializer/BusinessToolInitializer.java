package com.tianji.aigc.infrastructure.initializer;

import com.tianji.aigc.application.tool.service.BusinessToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessToolInitializer implements ApplicationRunner {

    private final BusinessToolRegistry businessToolRegistry;

    @Override
    public void run(ApplicationArguments args) {
        log.info("业务工具初始化完成，共注册 {} 个工具",
                businessToolRegistry.getAllToolDefinitions().size());
    }
}
