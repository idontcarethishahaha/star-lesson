package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.agent.dto.PageDTO;
import com.tianji.aigc.application.rag.dto.*;
import com.tianji.aigc.application.rag.service.RagAppService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagAppService ragAppService;

    @PostMapping("/datasets")
    public String createDataset(@RequestBody RagDatasetCreateRequest request) {
        String userId = getCurrentUserId();
        return ragAppService.createDataset(request, userId);
    }

    @PutMapping("/datasets/{id}")
    public void updateDataset(@PathVariable String id, @RequestBody RagDatasetUpdateRequest request) {
        ragAppService.updateDataset(id, request);
    }

    @DeleteMapping("/datasets/{id}")
    public void deleteDataset(@PathVariable String id) {
        ragAppService.deleteDataset(id);
    }

    @GetMapping("/datasets/{id}")
    public RagDatasetDTO getDatasetById(@PathVariable String id) {
        return ragAppService.getDatasetById(id);
    }

    @GetMapping("/datasets")
    public PageDTO<RagDatasetDTO> listDatasets(RagDatasetSearchRequest request) {
        return ragAppService.listDatasets(request);
    }

    @GetMapping("/datasets/all")
    public List<RagDatasetDTO> listAllDatasets() {
        String userId = getCurrentUserId();
        return ragAppService.listAllDatasets(userId);
    }

    @PostMapping("/datasets/{datasetId}/files")
    public String uploadFile(@PathVariable String datasetId,
                             @RequestParam("file") MultipartFile file) throws IOException {
        String userId = getCurrentUserId();
        return ragAppService.uploadFile(datasetId, file, userId);
    }

    @DeleteMapping("/datasets/{datasetId}/files/{fileId}")
    public void deleteFile(@PathVariable String datasetId,
                           @PathVariable String fileId) {
        ragAppService.deleteFile(datasetId, fileId);
    }

    @GetMapping("/datasets/{datasetId}/files")
    public List<FileDetailDTO> listFiles(@PathVariable String datasetId) {
        return ragAppService.listFiles(datasetId);
    }

    @GetMapping("/files/{fileId}/documents")
    public List<DocumentUnitDTO> listDocuments(@PathVariable String fileId) {
        return ragAppService.listDocuments(fileId);
    }

    @PostMapping("/datasets/{datasetId}/files/{fileId}/vectorize")
    public void vectorizeFile(@PathVariable String datasetId,
                              @PathVariable String fileId) {
        ragAppService.vectorizeFile(datasetId, fileId);
    }

    @PostMapping("/datasets/{ragId}/versions")
    public String createVersion(@PathVariable String ragId,
                                @RequestBody RagVersionCreateRequest request) {
        String userId = getCurrentUserId();
        return ragAppService.createVersion(request, userId);
    }

    @PostMapping("/versions/{versionId}/publish")
    public void publishVersion(@PathVariable String versionId) {
        ragAppService.publishVersion(versionId);
    }

    @GetMapping("/datasets/{ragId}/versions")
    public List<RagVersionDTO> listVersions(@PathVariable String ragId) {
        return ragAppService.listVersions(ragId);
    }

    @PostMapping("/search")
    public List<RagSearchResultDTO> search(@RequestBody RagSearchRequest request) {
        return ragAppService.search(request);
    }

    private String getCurrentUserId() {
        Long userId = UserContext.getUser();
        return userId != null ? String.valueOf(userId) : "1";
    }
}
