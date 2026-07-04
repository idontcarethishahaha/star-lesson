package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentVersionMapper extends BaseMapper<AgentVersionEntity> {
}
