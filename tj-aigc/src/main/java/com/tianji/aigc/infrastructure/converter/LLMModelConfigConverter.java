package com.tianji.aigc.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;
import com.tianji.aigc.domain.agent.model.LLMModelConfig;

@MappedTypes(LLMModelConfig.class)
public class LLMModelConfigConverter extends JsonToStringConverter<LLMModelConfig> {

    public LLMModelConfigConverter() {
        super(LLMModelConfig.class);
    }
}
