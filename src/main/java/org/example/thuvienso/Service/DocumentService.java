package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Request.DocumentSearchRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DocumentService {
    DocumentResponse create(DocumentRequest request);
    DocumentResponse getByIdResponse(String id);

    DocumentEntity getById(String id);
    List<DocumentResponseNoList> getListByIdFolder(String idFolder);
    DocumentResponse update(String idDocument, DocumentForm update);

    void deletedById(String id);
    List<DocumentResponse> getAllDocumentDeleted();
    DocumentResponse restoreDocument(String id);
    Page<DocumentResponse> getAllByPage(Pageable pageable);
    List<DocumentResponse> searchByTitleContainingIgnoreCase(String keyword);
    Page<DocumentResponse> searchAdvanced(DocumentSearchRequest req, Pageable pageable);
    List<DocumentResponse> getNewest(int limit);
    List<DocumentResponse> getMostViewed(int limit);
    List<DocumentResponse> getRelated(String idDocument, int limit);
}
