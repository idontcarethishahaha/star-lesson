package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.rag.model.RagVersionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RagVersionMapper extends BaseMapper<RagVersionEntity> {
}