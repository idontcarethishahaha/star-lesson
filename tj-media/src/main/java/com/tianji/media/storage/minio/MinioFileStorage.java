package com.tianji.media.storage.minio;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianji.common.exceptions.CommonException;
import com.tianji.media.storage.IFileStorage;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储实现
 */
@Slf4j
@Component
public class MinioFileStorage implements IFileStorage {

    private final MinioClient minioClient;

    public MinioFileStorage(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void uploadFile(String key, InputStream inputStream, long contentLength) {
        String bucketName = "tj-media";
        try {
            // 确保 bucket 存在
            ensureBucketExists(bucketName);

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .stream(inputStream, contentLength, -1)
                            .build()
            );
        } catch (Exception e) {
            log.error("上传文件失败: {}", key, e);
            throw new CommonException("上传文件失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        String bucketName = "tj-media";
        try {
            // 先确认对象是否存在，避免 getObject 在不存在时抛出异常
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(key)
                        .build());
            } catch (Exception ex) {
                // 对象不存在或其他访问错误，直接返回 null，交由上层处理
                log.warn("文件不存在或不可访问: {}", key);
                return null;
            }
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", key, e);
            return null;
        }
    }

    @Override
    public void deleteFile(String key) {
        String bucketName = "tj-media";
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败: {}", key, e);
            throw new CommonException("删除文件失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        String bucketName = "tj-media";
        try {
            for (String key : keys) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(key)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new CommonException("删除文件失败", e);
        }
    }

    /**
     * 获取文件访问 URL
     */
    private String getFileUrl(String bucketName, String key) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(key)
                            .expiry(7, TimeUnit.DAYS)  // 7天有效期
                            .build()
            );
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", key, e);
            throw new CommonException("生成文件URL失败", e);
        }
    }

    /**
     * 确保 bucket 存在，不存在则创建
     */
    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建 MinIO Bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建 Bucket 失败: {}", bucketName, e);
            throw new CommonException("初始化存储失败", e);
        }
    }
}
