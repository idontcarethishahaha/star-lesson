package com.tianji.aigc.domain.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.agent.model.AgentEntity;
import com.tianji.aigc.mapper.AgentMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends BaseMapper<AgentEntity> {
}
