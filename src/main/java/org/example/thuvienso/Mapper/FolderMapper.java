package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.FileRequest;
import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Dto.Response.Folder.ChildFolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Form.FolderForm;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Module.FolderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FolderMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "documentEntity",ignore = true)
    @Mapping(target = "parentFolder",ignore = true)
    @Mapping(target = "childFolder",ignore = true)
    FolderEntity toEntity(FolderRequest request);

    //    @Mapping(target = "role",ignore = true)
    FolderResponse toResponse(FolderEntity FolderEntity);

    ChildFolderResponse toChildResponse(FolderEntity FolderEntity);
    FolderResponseNoList toResponseNoList(FolderEntity FolderEntity);
    void update(@MappingTarget FolderEntity folder, FolderForm update);
}
