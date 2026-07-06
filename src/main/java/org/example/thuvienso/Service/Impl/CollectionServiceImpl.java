package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.CollectionRequest;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Enum.TypeCollection;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.CollectionMapper;
import org.example.thuvienso.Mapper.DocumentMapper;
import org.example.thuvienso.Module.CollectionEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.CollectionRepo;
import org.example.thuvienso.Service.CollectionService;
import org.example.thuvienso.Service.DocumentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollectionServiceImpl implements CollectionService {

    CollectionRepo collectionRepo;
    CollectionMapper collectionMapper;
    DocumentMapper documentMapper;
    DocumentService documentService;

    @Override
    public CollectionResponse create(CollectionRequest request) {

        CollectionEntity collection =
                collectionMapper.toEntity(request);

        collection.setCreatedAt(LocalDateTime.now());
        collection.setIsDeleted(false);

        return collectionMapper.toResponse(
                collectionRepo.save(collection)
        );
    }

    @Override
    public CollectionResponse getByIdResponse(String id) {

        return collectionMapper.toResponse(
                getById(id)
        );
    }

    @Override
    public CollectionEntity getById(String id) {

        return collectionRepo.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.COLLECTION_NOT_FOUND
                        )
                );
    }

    @Override
    public List<CollectionResponse> getAll() {

        return collectionRepo.findAll()
                .stream()
                .map(collectionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CollectionResponse update(
            String id,
            CollectionRequest request
    ) {

        CollectionEntity collection = getById(id);

        collection.setCollectionName(
                request.getCollectionName()
        );

        collection.setTypeCollection(
                TypeCollection.valueOf(request.getTypeCollection())
        );

        return collectionMapper.toResponse(
                collectionRepo.save(collection)
        );
    }

    @Override
    public void delete(String id) {

        CollectionEntity collection = getById(id);

        collection.setIsDeleted(true);

        collectionRepo.save(collection);
    }

    @Override
    public CollectionResponse addDocument(
            String idCollection,
            String idDocument
    ) {

        CollectionEntity collection =
                getById(idCollection);

        DocumentEntity document =
                documentService.getById(idDocument);

        collection.getDocuments().add(document);

        return collectionMapper.toResponse(
                collectionRepo.save(collection)
        );
    }

    @Override
    public CollectionResponse removeDocument(
            String idCollection,
            String idDocument
    ) {

        CollectionEntity collection =
                getById(idCollection);

        collection.getDocuments().removeIf(
                item ->
                        item.getIdDocument()
                                .equals(idDocument)
        );

        return collectionMapper.toResponse(
                collectionRepo.save(collection)
        );
    }

    @Override
    public List<DocumentResponseNoList> getDocuments(
            String idCollection
    ) {

        CollectionEntity collection =
                getById(idCollection);

        return collection.getDocuments()
                .stream()
                .map(documentMapper::toResponseNoList)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponse> getByType(
            String type
    ) {

        TypeCollection typeCollection =
                TypeCollection.valueOf(type);

        return collectionRepo
                .findByTypeCollection(typeCollection)
                .stream()
                .map(collectionMapper::toResponse)
                .collect(Collectors.toList());
    }

}
