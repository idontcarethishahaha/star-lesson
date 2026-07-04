package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.tool.model.ToolVersionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolVersionMapper extends BaseMapper<ToolVersionEntity> {
}
