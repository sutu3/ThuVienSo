package org.example.thuvienso.Dto.Response.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Dto.Response.File.FileResponseNoList;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.CollectionEntity;

import java.util.List;
import java.time.LocalDateTime;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentResponse {
    String idDocument;

    String content;

    String title;

    String summary;

    LocalDateTime publishedAt;

    String slug;

    StatusDocument status;

    TypeDocument typeDocument;

    String thumbnail;

    CategoryResponse categoryEntity;
    List<CollectionResponse> collections;


    FolderResponseNoList folderEntity;

    List<FileResponseNoList> fileEntity;
}
