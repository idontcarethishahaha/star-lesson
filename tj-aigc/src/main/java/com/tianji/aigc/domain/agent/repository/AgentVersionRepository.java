package com.tianji.aigc.domain.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.agent.model.AgentVersionEntity;
import com.tianji.aigc.mapper.AgentVersionMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentVersionRepository extends BaseMapper<AgentVersionEntity> {
}
