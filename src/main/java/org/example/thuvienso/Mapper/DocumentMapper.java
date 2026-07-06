package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Module.DocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "categoryEntity",ignore = true)
    @Mapping(target = "folderEntity",ignore = true)
    DocumentEntity toEntity(DocumentRequest request);

    //    @Mapping(target = "Document",ignore = true)
    DocumentResponse toResponse(DocumentEntity Entity);
    DocumentResponseNoList toResponseNoList(DocumentEntity Entity);
    void update(@MappingTarget DocumentEntity document, DocumentForm update);
}
