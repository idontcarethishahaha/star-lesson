package com.tianji.common.autoconfigure.mvc.advice;

import com.tianji.common.annotations.NoWrapper;
import com.tianji.common.constants.Constant;
import com.tianji.common.domain.R;
import com.tianji.common.utils.WebUtils;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@ConditionalOnClass({ResponseBodyAdvice.class})
public class WrapperResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if(methodParameter.hasMethodAnnotation(NoWrapper.class)){
            return false;
        }
        // 只包装来自网关的请求（前端调用），内部 Feign 调用不包装
        return methodParameter.getParameterType() != R.class && WebUtils.isGatewayRequest();
    }

    @Override
    public Object beforeBodyWrite(
            Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        if (request.getURI().getPath().equals("/v2/api-docs")){
            return body;
        }
        if (body == null) {
            return R.ok().requestId(MDC.get(Constant.REQUEST_ID_HEADER));
        }
        if(body instanceof R){
            return body;
        }
        return R.ok(body).requestId(MDC.get(Constant.REQUEST_ID_HEADER));
    }
}
