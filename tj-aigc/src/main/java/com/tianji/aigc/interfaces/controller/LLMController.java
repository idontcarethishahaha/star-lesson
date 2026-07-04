package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.llm.dto.*;
import com.tianji.aigc.application.llm.service.LLMAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LLMController {

    private final LLMAppService llmAppService;

    @PostMapping("/providers")
    public String createProvider(@RequestBody ProviderCreateRequest request) {
        return llmAppService.createProvider(request);
    }

    @PutMapping("/providers/{id}")
    public void updateProvider(@PathVariable String id, @RequestBody ProviderUpdateRequest request) {
        llmAppService.updateProvider(id, request);
    }

    @DeleteMapping("/providers/{id}")
    public void deleteProvider(@PathVariable String id) {
        llmAppService.deleteProvider(id);
    }

    @GetMapping("/providers/{id}")
    public ProviderDTO getProviderById(@PathVariable String id) {
        return llmAppService.getProviderById(id);
    }

    @GetMapping("/providers")
    public PageDTO<ProviderDTO> listProviders(ProviderSearchRequest request) {
        return llmAppService.listProviders(request);
    }

    @GetMapping("/providers/all")
    public List<ProviderDTO> listAllProviders() {
        return llmAppService.listAllProviders();
    }

    @PostMapping("/models")
    public String createModel(@RequestBody ModelCreateRequest request) {
        return llmAppService.createModel(request);
    }

    @PutMapping("/models/{id}")
    public void updateModel(@PathVariable String id, @RequestBody ModelUpdateRequest request) {
        llmAppService.updateModel(id, request);
    }

    @DeleteMapping("/models/{id}")
    public void deleteModel(@PathVariable String id) {
        llmAppService.deleteModel(id);
    }

    @GetMapping("/models/{id}")
    public ModelDTO getModelById(@PathVariable String id) {
        return llmAppService.getModelById(id);
    }

    @GetMapping("/models")
    public PageDTO<ModelDTO> listModels(ModelSearchRequest request) {
        return llmAppService.listModels(request);
    }

    @GetMapping("/models/by-provider/{providerId}")
    public List<ModelDTO> listModelsByProviderId(@PathVariable String providerId) {
        return llmAppService.listModelsByProviderId(providerId);
    }

    @GetMapping("/models/chat")
    public List<ModelDTO> listAllChatModels() {
        return llmAppService.listAllChatModels();
    }

    @GetMapping("/models/embedding")
    public List<ModelDTO> listAllEmbeddingModels() {
        return llmAppService.listAllEmbeddingModels();
    }

    @PostMapping("/models/{id}/enable")
    public void enableModel(@PathVariable String id) {
        llmAppService.enableModel(id);
    }

    @PostMapping("/models/{id}/disable")
    public void disableModel(@PathVariable String id) {
        llmAppService.disableModel(id);
    }
}
