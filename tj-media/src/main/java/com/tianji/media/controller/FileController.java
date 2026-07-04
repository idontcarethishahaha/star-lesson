package com.tianji.media.controller;

import com.tianji.common.utils.StringUtils;
import com.tianji.media.domain.dto.FileDTO;
import com.tianji.media.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件表，可以是普通文件、图片等 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "媒资管理相关接口")
public class FileController {

    private final IFileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping
    public FileDTO uploadFile(
            @Parameter(description = "文件数据") @RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @Operation(summary = "上传头像（文件保存在 img-tx/ 前缀下）")
    @PostMapping("/avatar")
    public FileDTO uploadAvatar(
            @Parameter(description = "头像图片") @RequestParam("file") MultipartFile file) {
        return fileService.uploadAvatar(file);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{id:\\d+}")
    public FileDTO getFileInfo(
            @Parameter(description = "文件id", example = "1") @PathVariable("id") Long id) {
        return fileService.getFileInfo(id);
    }

    @Operation(summary = "公开访问文件（通过存储 key 读取文件流）")
    @GetMapping("/public/{key}")
    public ResponseEntity<byte[]> getFileByKeyPublic(@PathVariable("key") String key) {
        return getFileByKeyInternal(key);
    }

    /**
     * 公开访问文件（无 /public/ 前缀）
     * 网关 StripPrefix=0，直接将 /files/{key} 转发到这里
     * 注意：key 必须包含文件扩展名（如 .jpg, .png），避免和 /{id:\d+} 冲突
     */
    @GetMapping("/{key:.*\\.[a-zA-Z0-9]+}")
    public ResponseEntity<byte[]> getFileByKey(@PathVariable("key") String key) {
        return getFileByKeyInternal(key);
    }

    private ResponseEntity<byte[]> getFileByKeyInternal(String key) {
        if (StringUtils.isBlank(key)) {
            return placeholderResponse();
        }
        try (InputStream inputStream = fileService.downloadByKey(key)) {
            if (inputStream == null) {
                return placeholderResponse();
            }
            byte[] bytes = inputStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            String lower = key.toLowerCase();
            if (lower.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (lower.endsWith(".gif")) {
                headers.setContentType(MediaType.IMAGE_GIF);
            } else if (lower.endsWith(".webp")) {
                headers.setContentType(new MediaType("image", "webp"));
            } else if (lower.endsWith(".svg")) {
                headers.setContentType(new MediaType("image", "svg+xml"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.setContentLength(bytes.length);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.warn("读取文件失败: {}", key, e);
            return placeholderResponse();
        }
    }

    /**
     * 返回一张 1x1 透明 GIF 占位图，避免前端图片区域出现裂图。
     */
    private static ResponseEntity<byte[]> placeholderResponse() {
        byte[] gif = new byte[] {
                0x47, 0x49, 0x46, 0x38, 0x39, 0x61, // GIF89a
                0x01, 0x00, 0x01, 0x00,             // 1x1 像素
                (byte) 0x80, 0x00, 0x00,             // 全局颜色表
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                0x00, 0x00, 0x00,
                0x21, (byte) 0xF9, 0x04,             // 图形控制扩展
                0x01, 0x00, 0x00, 0x00,
                0x2C, 0x00, 0x00, 0x00, 0x00,       // 图像描述符
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x02, 0x02, 0x44, 0x01, 0x00,
                0x3B                                 // 结束
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_GIF);
        headers.setContentLength(gif.length);
        headers.setCacheControl("no-store");
        return new ResponseEntity<>(gif, headers, HttpStatus.OK);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id:\\d+}")
    public void deleteFileById(
            @Parameter(description = "文件id", example = "1") @PathVariable("id") Long id) {
        fileService.removeById(id);
    }
}
