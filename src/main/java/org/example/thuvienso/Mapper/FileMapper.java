package org.example.thuvienso.Mapper;


import org.example.thuvienso.Dto.Request.FileRequest;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Module.FileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "documentEntity",ignore = true)
    FileEntity toEntity(FileRequest request);

    //    @Mapping(target = "role",ignore = true)
    FileResponse toResponse(FileEntity FileEntity);
}
