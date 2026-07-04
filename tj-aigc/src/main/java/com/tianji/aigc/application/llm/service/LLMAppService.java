package com.tianji.aigc.application.llm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.llm.assembler.LLMAssembler;
import com.tianji.aigc.application.llm.dto.*;
import com.tianji.aigc.domain.llm.model.ModelEntity;
import com.tianji.aigc.domain.llm.model.ProviderEntity;
import com.tianji.aigc.domain.llm.model.enums.ModelType;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.exception.EntityNotFoundException;
import com.tianji.aigc.mapper.ModelMapper;
import com.tianji.aigc.mapper.ProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LLMAppService {

    private final ProviderMapper providerMapper;
    private final ModelMapper modelMapper;

    @Transactional
    public String createProvider(ProviderCreateRequest request) {
        ProviderEntity entity = LLMAssembler.toProviderEntity(request);
        providerMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateProvider(String id, ProviderUpdateRequest request) {
        ProviderEntity entity = providerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("服务商不存在");
        }
        LLMAssembler.updateProviderEntity(entity, request);
        providerMapper.updateById(entity);
    }

    @Transactional
    public void deleteProvider(String id) {
        ProviderEntity entity = providerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("服务商不存在");
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getProviderId, id);
        long modelCount = modelMapper.selectCount(wrapper);
        if (modelCount > 0) {
            throw new BusinessException("该服务商下存在模型，无法删除");
        }
        providerMapper.deleteById(id);
    }

    public ProviderDTO getProviderById(String id) {
        ProviderEntity entity = providerMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("服务商不存在");
        }
        return LLMAssembler.toProviderDTO(entity);
    }

    public PageDTO<ProviderDTO> listProviders(ProviderSearchRequest request) {
        Page<ProviderEntity> page = new Page<>(request.getPageNo(), request.getPageSize());
        LambdaQueryWrapper<ProviderEntity> wrapper = new LambdaQueryWrapper<>();
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(ProviderEntity::getName, request.getName());
        }
        if (request.getProtocol() != null && !request.getProtocol().isEmpty()) {
            wrapper.eq(ProviderEntity::getProtocol, request.getProtocol());
        }
        if (request.getIsOfficial() != null) {
            wrapper.eq(ProviderEntity::getIsOfficial, request.getIsOfficial());
        }
        wrapper.orderByDesc(ProviderEntity::getCreatedAt);

        Page<ProviderEntity> result = providerMapper.selectPage(page, wrapper);
        PageDTO<ProviderDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(LLMAssembler::toProviderDTO)
                .collect(Collectors.toList()));
        return pageDTO;
    }

    public List<ProviderDTO> listAllProviders() {
        LambdaQueryWrapper<ProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderEntity::getStatus, true);
        wrapper.orderByDesc(ProviderEntity::getIsOfficial);
        wrapper.orderByAsc(ProviderEntity::getName);
        return providerMapper.selectList(wrapper).stream()
                .map(LLMAssembler::toProviderDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createModel(ModelCreateRequest request) {
        ProviderEntity provider = providerMapper.selectById(request.getProviderId());
        if (provider == null) {
            throw new EntityNotFoundException("服务商不存在");
        }
        ModelEntity entity = LLMAssembler.toModelEntity(request);
        modelMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void updateModel(String id, ModelUpdateRequest request) {
        ModelEntity entity = modelMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("模型不存在");
        }
        LLMAssembler.updateModelEntity(entity, request);
        modelMapper.updateById(entity);
    }

    @Transactional
    public void deleteModel(String id) {
        ModelEntity entity = modelMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("模型不存在");
        }
        modelMapper.deleteById(id);
    }

    public ModelDTO getModelById(String id) {
        ModelEntity entity = modelMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("模型不存在");
        }
        ModelDTO dto = LLMAssembler.toModelDTO(entity);
        ProviderEntity provider = providerMapper.selectById(entity.getProviderId());
        if (provider != null) {
            dto.setProviderName(provider.getName());
        }
        return dto;
    }

    public PageDTO<ModelDTO> listModels(ModelSearchRequest request) {
        Page<ModelEntity> page = new Page<>(request.getPageNo(), request.getPageSize());
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        if (request.getProviderId() != null && !request.getProviderId().isEmpty()) {
            wrapper.eq(ModelEntity::getProviderId, request.getProviderId());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            wrapper.like(ModelEntity::getName, request.getName());
        }
        if (request.getType() != null) {
            wrapper.eq(ModelEntity::getType, request.getType());
        }
        if (request.getIsOfficial() != null) {
            wrapper.eq(ModelEntity::getOfficial, request.getIsOfficial());
        }
        wrapper.orderByDesc(ModelEntity::getCreatedAt);

        Page<ModelEntity> result = modelMapper.selectPage(page, wrapper);
        PageDTO<ModelDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPageNo((int) result.getCurrent());
        pageDTO.setPageSize((int) result.getSize());
        pageDTO.setList(result.getRecords().stream()
                .map(LLMAssembler::toModelDTO)
                .collect(Collectors.toList()));
        return pageDTO;
    }

    public List<ModelDTO> listModelsByProviderId(String providerId) {
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getProviderId, providerId);
        wrapper.eq(ModelEntity::getStatus, true);
        wrapper.orderByDesc(ModelEntity::getCreatedAt);
        return modelMapper.selectList(wrapper).stream()
                .map(LLMAssembler::toModelDTO)
                .collect(Collectors.toList());
    }

    public List<ModelDTO> listAllChatModels() {
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getType, ModelType.CHAT);
        wrapper.eq(ModelEntity::getStatus, true);
        wrapper.orderByDesc(ModelEntity::getOfficial);
        wrapper.orderByAsc(ModelEntity::getName);
        return modelMapper.selectList(wrapper).stream()
                .map(LLMAssembler::toModelDTO)
                .collect(Collectors.toList());
    }

    public List<ModelDTO> listAllEmbeddingModels() {
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getType, ModelType.EMBEDDING);
        wrapper.eq(ModelEntity::getStatus, true);
        wrapper.orderByDesc(ModelEntity::getOfficial);
        wrapper.orderByAsc(ModelEntity::getName);
        return modelMapper.selectList(wrapper).stream()
                .map(LLMAssembler::toModelDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void enableModel(String id) {
        ModelEntity entity = modelMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("模型不存在");
        }
        entity.setStatus(true);
        modelMapper.updateById(entity);
    }

    @Transactional
    public void disableModel(String id) {
        ModelEntity entity = modelMapper.selectById(id);
        if (entity == null) {
            throw new EntityNotFoundException("模型不存在");
        }
        entity.setStatus(false);
        modelMapper.updateById(entity);
    }
}
