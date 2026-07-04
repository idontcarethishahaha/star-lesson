package com.tianji.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.SensitiveWordDTO;
import com.tianji.message.domain.dto.SensitiveWordFormDTO;
import com.tianji.message.domain.po.SensitiveWord;
import com.tianji.message.domain.query.SensitiveWordPageQuery;

public interface ISensitiveWordService extends IService<SensitiveWord> {
    void saveSensitiveWord(SensitiveWordFormDTO sensitiveWordFormDTO);

    void updateSensitiveWord(SensitiveWordFormDTO sensitiveWordFormDTO);

    PageDTO<SensitiveWordDTO> listSensitiveWords(SensitiveWordPageQuery pageQuery);

    void deleteSensitiveWord(Long id);
}