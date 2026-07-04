export const prefix = '';
export const TOKEN_NAME = 'token';

/**
 * 开发环境 API 基础地址
 * - 所有请求应该走网关（端口 10010），再由网关转发到具体服务
 * - chat-service: 8095，aigc-service: 8092/8094，media-service: 8084，user-service: 8081
 */
export const devAPIHost = 'http://localhost:10010';
