package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.CollectionRequest;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Module.CollectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "documents",ignore = true)
    CollectionEntity toEntity(CollectionRequest request);

    //    @Mapping(target = "Collection",ignore = true)
    CollectionResponse toResponse(CollectionEntity Entity);
}
