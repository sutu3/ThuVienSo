package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Service.FileService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BuildPath {
    public String buildObjectPath(
            MultipartFile file
    ) {

        String contentType = file.getContentType();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + file.getOriginalFilename();
        if (contentType == null) {
            return "others/" + fileName;
        }

        if (contentType.startsWith("image/")) {
            return "images/" + fileName;
        }

        if (contentType.startsWith("video/")) {
            return "videos/" + fileName;
        }

        if (contentType.equals("application/pdf")) {
            return "documents/pdf/" + fileName;
        }

        if (contentType.contains("word")) {
            return "documents/docx/" + fileName;
        }

        return "others/" + fileName;
    }
}
