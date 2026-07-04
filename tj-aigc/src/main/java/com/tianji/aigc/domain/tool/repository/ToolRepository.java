package com.tianji.aigc.domain.tool.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import com.tianji.aigc.mapper.ToolMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolRepository extends BaseMapper<ToolEntity> {
}
