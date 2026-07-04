package com.tianji.aigc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.MinioProperties;
import com.tianji.aigc.service.FileStorage;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.AssertUtils;
import com.tianji.common.utils.CollUtils;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorage implements FileStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength) {
        var bucketName = minioProperties.getBucket();
        AssertUtils.isNotBlank(bucketName, "Bucket名称为空");
        AssertUtils.isNotBlank(key, "文件Key为空");
        AssertUtils.isNotNull(inputStream, "文件流为空");

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

            // 返回访问 URL
            return getFileUrl(bucketName, key);
        } catch (Exception e) {
            log.error("上传文件[{}]失败", key, e);
            throw new CommonException("上传文件失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        var bucketName = minioProperties.getBucket();
        AssertUtils.isNotBlank(bucketName, "Bucket名称为空");
        AssertUtils.isNotBlank(key, "文件Key为空");

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件[{}]失败", key, e);
            throw new CommonException("文件下载失败", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        var bucketName = minioProperties.getBucket();
        AssertUtils.isNotBlank(bucketName, "Bucket名称为空");
        AssertUtils.isNotBlank(key, "文件Key为空");

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件[{}]失败", key, e);
            throw new CommonException("文件删除失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> keys) {
        if (CollUtils.isEmpty(keys)) {
            return;
        }
        var bucketName = minioProperties.getBucket();
        AssertUtils.isNotBlank(bucketName, "Bucket名称为空");

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
            throw new CommonException("文件删除失败", e);
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
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", key, e);
            throw new CommonException("生成文件URL失败", e);
        }
    }

    /**
     * 确保 bucket 存在
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
