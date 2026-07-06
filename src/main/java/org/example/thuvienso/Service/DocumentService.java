package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DocumentService {
    DocumentResponse create(DocumentRequest request);
    DocumentResponse getByIdResponse(String id);

    DocumentEntity getById(String id);
    List<DocumentResponseNoList> getListByIdFolder(String idFolder);
    DocumentResponse update(String idDocument, DocumentForm update);

}
