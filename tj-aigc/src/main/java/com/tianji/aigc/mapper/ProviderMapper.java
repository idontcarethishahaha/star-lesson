package com.tianji.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.aigc.domain.llm.model.ProviderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProviderMapper extends BaseMapper<ProviderEntity> {
}
