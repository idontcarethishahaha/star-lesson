package com.tianji.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.aigc.domain.po.MarkdownDocs;
import com.tianji.aigc.domain.vo.MarkdownChunkVO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户上传的 Markdown 文档表 服务类
 *
 * @author lusy
 */
public interface IMarkdownDocsService extends IService<MarkdownDocs> {

    /**
     * 上传 Markdown 文件
     *
     * @param file  文件
     * @param level 切割等级
     * @return MarkdownDocs
     */
    MarkdownDocs upload(MultipartFile file, Integer level);

    /**
     * 获取 Markdown 文件内容
     *
     * @param fileId 文件ID
     * @return 文件内容
     */
    String getMarkdown(Long fileId);

    /**
     * 分页查询 Markdown 文件列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageDTO<MarkdownDocs> queryMarkdownPage(PageQuery query);

    /**
     * 根据知识库内容对话
     *
     * @param message 用户问题
     * @return 匹配的文档块
     */
    List<MarkdownChunkVO> chatByMarkdownDoc(String message);

    /**
     * 更新 Markdown 文件
     *
     * @param markdownDocs 文件信息
     */
    void updateMarkdown(MarkdownDocs markdownDocs);

    /**
     * 删除 Markdown 文件
     *
     * @param fileId 文件ID
     */
    void removeMarkdown(Long fileId);
}
