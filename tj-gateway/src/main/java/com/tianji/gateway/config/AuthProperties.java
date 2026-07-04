package com.tianji.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "tj.auth")
public class AuthProperties implements InitializingBean {

    private Set<String> excludePath = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加默认不拦截的路径
        excludePath.add("/error/**");
        excludePath.add("/jwks");
        excludePath.add("GET:/accounts/login");
        excludePath.add("POST:/accounts/login");
        excludePath.add("GET:/accounts/admin/login");
        excludePath.add("POST:/accounts/admin/login");
        excludePath.add("GET:/accounts/refresh");
        excludePath.add("POST:/accounts/refresh");
        // 文件访问路径
        excludePath.add("GET:/files/**");
        excludePath.add("POST:/files/**");
        excludePath.add("GET:/ms/files/**");
        excludePath.add("POST:/ms/files/**");
        // 头像路径
        excludePath.add("GET:/img-tx/**");
        // AI会话相关路径
        excludePath.add("GET:/session/**");
        excludePath.add("POST:/session/**");
        excludePath.add("GET:/chat/**");
        excludePath.add("POST:/chat/**");
    }
}
