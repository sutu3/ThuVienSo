package org.example.thuvienso.Service.Impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Helper.BuildPath;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.MinioService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;
    GetUrl getUrl;
    BuildPath buildPath;
    FileRepo fileRepo;

    @Override
    public FileUploadResponse upload(
            MultipartFile file
    ) throws Exception {

        String objectName =
                buildPath.buildObjectPath(file);
        log.warn(objectName);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("thuvienso")
                        .object(objectName)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );

        String url = getUrl.getFileUrl(objectName);

        return FileUploadResponse.builder()
                .objectName(objectName)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .previewUrl(url)
                .build();
    }
}
