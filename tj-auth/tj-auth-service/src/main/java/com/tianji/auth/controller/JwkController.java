package com.tianji.auth.controller;

import cn.hutool.core.codec.Base64;
import com.tianji.common.annotations.NoWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;

@Hidden
@RestController
@RequestMapping("jwks")
public class JwkController {

    private final KeyPair keyPair;

    @Autowired
    public JwkController(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @NoWrapper
    @GetMapping
    public String getJwk(){
        // TODO 可以加入clientId和clientSecret校验
        // 获取公钥并转码
        return Base64.encode(keyPair.getPublic().getEncoded());
    }
}
