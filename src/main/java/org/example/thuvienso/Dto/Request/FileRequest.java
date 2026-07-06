package org.example.thuvienso.Dto.Request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.TypeFile;
import org.example.thuvienso.Module.BaseEntity;
import org.example.thuvienso.Module.DocumentEntity;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileRequest {

    String fileName;

    String partFile;

    TypeFile typeFile;

    String thumbnail;

    String documentEntity;
}
