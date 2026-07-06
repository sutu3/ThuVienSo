package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Mapper.DocumentMapper;
import org.example.thuvienso.Module.CategoryEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FolderEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.DocumentService;
import org.example.thuvienso.Service.FolderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepo documentRepo;
    DocumentMapper documentMapper;
    CategoryService categoryService;
    FolderService folderService;
    @Override
    public DocumentResponse create(DocumentRequest request) {
        DocumentEntity document=documentMapper.toEntity(request);
        CategoryEntity category=categoryService.getById(request.getCategoryEntity());
        FolderEntity folder=folderService.getById(request.getFolderEntity());
        document.setCategoryEntity(category);
        document.setFolderEntity(folder);
        document.setCreatedAt(LocalDateTime.now());
        document.setIsDeleted(false);
        documentRepo.save(document);
        return documentMapper.toResponse(document);
    }

    @Override
    public DocumentResponse getByIdResponse(String id) {
        return documentMapper.toResponse(getById(id));
    }

    @Override
    public DocumentEntity getById(String id) {
        return documentRepo.findById(id)
                .orElseThrow(()->new AppException(
                        ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Override
    public List<DocumentResponseNoList> getListByIdFolder(String idFolder) {
        return documentRepo.findByFolderEntity_IdFolder(idFolder)
                .stream().map(documentMapper::toResponseNoList)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentResponse update(String idDocument, DocumentForm update) {
        DocumentEntity document=getById(idDocument);
        documentMapper.update(document,update);

        return documentMapper.toResponse(documentRepo.save(document));
    }
}
