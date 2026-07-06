package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.CollectionRequest;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Module.CollectionEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CollectionService {
    CollectionResponse create(CollectionRequest request);
    CollectionResponse getByIdResponse(String id);
    CollectionEntity getById(String id);
    List<CollectionResponse> getAll();
    CollectionResponse update(
            String id,
            CollectionRequest request
    );
    void delete(String id);
    CollectionResponse addDocument(
            String idCollection,
            String idDocument
    );
    CollectionResponse removeDocument(
            String idCollection,
            String idDocument
    );
    List<DocumentResponseNoList> getDocuments(
            String idCollection
    );
    List<CollectionResponse> getByType(
            String type
    );
}
