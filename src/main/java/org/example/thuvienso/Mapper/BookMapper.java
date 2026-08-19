package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.BookRequest;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Form.BookForm;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "idBook", ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    @Mapping(target = "categoryEntity", ignore = true)
    @Mapping(target = "documentEntity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    BookEntity toEntity(BookRequest request);

    @Mapping(source = "documentEntity", target = "document")
    @Mapping(source = "documentEntity.thumbnail", target = "thumbnail")
    BookResponse toResponse(BookEntity entity);

    void update(
            @MappingTarget BookEntity book,
            BookForm form
    );



}