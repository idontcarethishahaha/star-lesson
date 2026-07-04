package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.rag.model.DocumentUnitEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentUnitMapper extends BaseMapper<DocumentUnitEntity> {
}