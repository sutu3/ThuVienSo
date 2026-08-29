package org.example.thuvienso.Dto.Request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Dto.Response.File.FileResponseNoList;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.*;

import java.util.List;
import java.time.LocalDateTime;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRequest {
    String content;

    String title;

    String summary;

    LocalDateTime publishedAt;

    String slug;

    String status;

    String typeDocument;

    String thumbnail;

    String categoryEntity;


    String folderEntity;
}
