package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.message.domain.dto.SensitiveWordDTO;
import com.tianji.message.domain.dto.SensitiveWordFormDTO;
import com.tianji.message.domain.po.SensitiveWord;
import com.tianji.message.domain.query.SensitiveWordPageQuery;
import com.tianji.message.mapper.SensitiveWordMapper;
import com.tianji.message.service.ISensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements ISensitiveWordService {

    @Override
    public void saveSensitiveWord(SensitiveWordFormDTO sensitiveWordFormDTO) {
        SensitiveWord sensitiveWord = BeanUtils.copyBean(sensitiveWordFormDTO, SensitiveWord.class);
        this.save(sensitiveWord);
    }

    @Override
    public void updateSensitiveWord(SensitiveWordFormDTO sensitiveWordFormDTO) {
        SensitiveWord sensitiveWord = BeanUtils.copyBean(sensitiveWordFormDTO, SensitiveWord.class);
        this.updateById(sensitiveWord);
    }

    @Override
    public PageDTO<SensitiveWordDTO> listSensitiveWords(SensitiveWordPageQuery pageQuery) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(pageQuery.getKeyword())) {
            wrapper.like(SensitiveWord::getSensitives, pageQuery.getKeyword());
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);

        Page<SensitiveWord> page = this.page(pageQuery.toMpPage(), wrapper);
        return PageDTO.of(page, SensitiveWordDTO.class);
    }

    @Override
    public void deleteSensitiveWord(Long id) {
        this.removeById(id);
    }
}