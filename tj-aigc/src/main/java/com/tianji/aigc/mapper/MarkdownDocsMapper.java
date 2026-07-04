package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.po.MarkdownDocs;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户上传的 Markdown 文档表 Mapper 接口
 *
 * @author lusy
 * @since 2025-05-07
 */
@Mapper
public interface MarkdownDocsMapper extends BaseMapper<MarkdownDocs> {

}
