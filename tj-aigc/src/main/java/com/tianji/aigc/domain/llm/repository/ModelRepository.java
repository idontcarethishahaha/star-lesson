package com.tianji.aigc.domain.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.llm.model.ModelEntity;
import com.tianji.aigc.mapper.ModelMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends BaseMapper<ModelEntity> {
}
