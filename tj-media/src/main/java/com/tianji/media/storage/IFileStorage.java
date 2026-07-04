package com.tianji.media.storage;

import java.io.InputStream;
import java.util.List;

public interface IFileStorage {

    /**
     * 上传文件
     * @param key 文件唯一标识（a.jpg 或 img-tx/a.jpg)
     * @param inputStream 文件流
     */
    void uploadFile(String key, InputStream inputStream, long contentLength);

    /**
     * 下载文件
     * @param key 文件唯一标识（a.jpg 或 img-tx/a.jpg)
     * @return 文件流
     */
    InputStream downloadFile(String key);

    /**
     * 删除指定文件
     * @param key 文件唯一标识
     */
    void deleteFile(String key);

    /**
     * 批量删除指定文件
     * @param keys 文件唯一标识集合
     */
    void deleteFiles(List<String> keys);
}
