package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolMapper extends BaseMapper<ToolEntity> {
}
