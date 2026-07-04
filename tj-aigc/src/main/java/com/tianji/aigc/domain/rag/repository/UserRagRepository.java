package com.tianji.aigc.domain.rag.repository;

import org.apache.ibatis.annotations.Mapper;
import com.tianji.aigc.domain.rag.model.UserRagEntity;
import com.tianji.aigc.infrastructure.repository.MyBatisPlusExtRepository;

@Mapper
public interface UserRagRepository extends MyBatisPlusExtRepository<UserRagEntity> {

}
