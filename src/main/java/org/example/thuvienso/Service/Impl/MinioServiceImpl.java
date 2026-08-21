package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Helper.BuildPath;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.LocalStorage;
import org.example.thuvienso.Service.MinioService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioServiceImpl implements MinioService {

    LocalStorage localStorage;
    GetUrl getUrl;
    BuildPath buildPath;

    @Override
    public FileUploadResponse upload(MultipartFile file) throws Exception {
        String objectName = buildPath.buildObjectPath(file);
        try (InputStream in = file.getInputStream()) {
            localStorage.store(objectName.getBytes(), in.toString());
        }
        return FileUploadResponse.builder()
                .objectName(objectName)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .previewUrl(getUrl.getFileUrl(objectName))
                .build();
    }
}