package org.example.thuvienso.Dto.Response.File;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.TypeFile;
import org.example.thuvienso.Module.DocumentEntity;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponseNoList {
    String idFile;

    String fileName;

    String partFile;

    TypeFile typeFile;

    String thumbnail;

}
