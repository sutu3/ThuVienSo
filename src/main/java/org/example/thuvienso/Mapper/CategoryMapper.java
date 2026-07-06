package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.CategoryRequest;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Module.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "documentEntity",ignore = true)
    CategoryEntity toEntity(CategoryRequest request);

    //    @Mapping(target = "Category",ignore = true)
    CategoryResponse toResponse(CategoryEntity Entity);
}
