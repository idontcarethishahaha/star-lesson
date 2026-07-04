package com.tianji.aigc.domain.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.llm.model.ProviderEntity;
import com.tianji.aigc.mapper.ProviderMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends BaseMapper<ProviderEntity> {
}
