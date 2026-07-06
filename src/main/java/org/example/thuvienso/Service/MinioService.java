package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public interface MinioService {
    FileUploadResponse upload(
            MultipartFile file
    ) throws Exception;
}
