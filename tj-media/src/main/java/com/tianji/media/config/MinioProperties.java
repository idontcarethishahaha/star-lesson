package com.tianji.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 配置属性
 */
@Data
@ConfigurationProperties(prefix = "tj.minio")
public class MinioProperties {

    /**
     * 是否启用 MinIO
     */
    private boolean enabled = false;

    /**
     * MinIO 服务地址
     */
    private String endpoint = "http://localhost:9000";

    /**
     * MinIO Access Key
     */
    private String accessKey = "minioadmin";

    /**
     * MinIO Secret Key
     */
    private String secretKey = "minioadmin";

    /**
     * Bucket 名称
     */
    private String bucket = "tj-media";
}
