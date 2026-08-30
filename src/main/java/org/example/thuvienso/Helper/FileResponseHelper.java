package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Enum.TypeFile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileResponseHelper {
    private final GetUrl getUrl;

    public FileResponse buildUrl(FileResponse response) {

        try {

            if (response.getPartFile() != null
                    && !response.getPartFile().isBlank()) {

                response.setPartFile(
                        getUrl.getFileUrl(response.getPartFile())
                );
            }

            if (response.getThumbnail() != null
                    && !response.getThumbnail().isBlank()) {

                response.setThumbnail(
                        getUrl.getFileUrl(response.getThumbnail())
                );
            }

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Không thể tạo URL cho file: "
                            + response.getFileName(),
                    e
            );
        }
    }
    public TypeDocument mapFileToDocumentType(TypeFile typeFile) {
        return switch (typeFile) {
            case MP4        -> TypeDocument.VIDEO;
            case MP3        -> TypeDocument.AUDIO;
            case PDF        -> TypeDocument.PDF;
            case PNG, JPG   -> TypeDocument.IMAGE;
            default         -> TypeDocument.DOCUMENT;   // DOCX, ZIP...
        };
    }
}
