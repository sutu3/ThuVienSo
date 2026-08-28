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
        return upload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
    }

    @Override
    public FileUploadResponse upload(byte[] content, String originalFileName, String contentType) throws Exception {
        String objectName = buildPath.buildObjectPath(originalFileName, contentType);
        localStorage.store(content, objectName);
        return FileUploadResponse.builder()
                .objectName(objectName)
                .originalFileName(originalFileName)
                .contentType(contentType)
                .size((long) content.length)
                .previewUrl(getUrl.getFileUrl(objectName))
                .build();
    }
}
