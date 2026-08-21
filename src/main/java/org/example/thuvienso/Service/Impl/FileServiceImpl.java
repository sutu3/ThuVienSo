package org.example.thuvienso.Service.Impl;

import io.minio.*;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Enum.TypeFile;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.LocalStorage;
import org.example.thuvienso.Helper.ThumbnailGenerator;
import org.example.thuvienso.Mapper.FileMapper;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.DocumentService;
import org.example.thuvienso.Service.FileService;
import org.example.thuvienso.Service.MinioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {


    private final DocumentRepo documentRepo;
    DocumentService documentService;
    FileRepo fileRepo;
    MinioService minioService;
    FileMapper fileMapper;
    MinioClient minioClient;
    ThumbnailGenerator thumbnailGenerator;
    LocalStorage localStorage;
    GetUrl getUrl;

    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file, String idDocument) throws Exception {
        if (file.getSize() > 100L * 1024 * 1024) throw new AppException(ErrorCode.FILE_IS_TO_BIG);

        FileUploadResponse uploaded = minioService.upload(file);
        DocumentEntity document = documentService.getById(idDocument);

        // Toàn bộ logic thumbnail nằm trong helper
        String thumbObject = thumbnailGenerator.applyThumbnail(file, document);

        FileEntity fileEntity = FileEntity.builder()
                .partFile(uploaded.getObjectName())
                .fileName(uploaded.getOriginalFileName())
                .typeFile(TypeFile.fromMimeType(uploaded.getContentType()))
                .thumbnail(thumbObject)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .documentEntity(document)
                .build();

        return fileMapper.toResponse(fileRepo.save(fileEntity));
    }

    @Override
    public ResponseEntity<InputStreamResource> viewFile(String id) throws IOException {
        FileEntity fileEntity = fileRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        InputStream stream = localStorage.load(fileEntity.getPartFile());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getTypeFile().getMimeType()))
                .body(new InputStreamResource(stream));
    }
    @Override
    public ResponseEntity<InputStreamResource> dowloadFile(String id) throws IOException {
        FileEntity fileEntity = fileRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        InputStream stream = localStorage.load(fileEntity.getPartFile());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getTypeFile().getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .body(new InputStreamResource(stream));
    }
    @Override
    public FileEntity getByFileName(String fileName) {
        return fileRepo.findByFileName(fileName)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
    }


    @Override
    public List<FileResponse> getByIdDocument(String idDocument) {
        return fileRepo.findByDocumentEntity_IdDocument(idDocument)
                .stream()
                .map(fileMapper::toResponse)
                .map(response -> {
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
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Không thể tạo URL cho file: "
                                        + response.getFileName(),
                                e
                        );
                    }

                    return response;
                })
                .toList();
    }

    // src/main/java/org/example/thuvienso/Service/Impl/FileServiceImpl.java
    @Override
    public ResponseEntity<InputStreamResource> streamFile(String id, String rangeHeader) throws Exception {
        FileEntity fileEntity = fileRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        String objectName = fileEntity.getPartFile();
        long fileSize = localStorage.size(objectName);
        String contentType = fileEntity.getTypeFile().getMimeType();

        long start = 0, end = fileSize - 1;
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) end = Long.parseLong(ranges[1]);
        }
        long length = end - start + 1;
        InputStream stream = localStorage.loadRange(objectName, start, length);

        return ResponseEntity.status(rangeHeader == null ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length))
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(stream));
    }
    @Override
    public void deleteFile(String id) throws IOException {
        FileEntity fileEntity = fileRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        localStorage.delete(fileEntity.getPartFile());
        fileRepo.delete(fileEntity);
    }

    @Override
    public ResponseEntity<InputStreamResource> viewThumbnail(String objectName) throws Exception {
        if (objectName == null || objectName.isBlank())
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        InputStream stream = localStorage.load(objectName);
        MediaType type = objectName.toLowerCase().endsWith(".png")
                ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;   // icon .png, thumbnail .jpg
        return ResponseEntity.ok().contentType(type).body(new InputStreamResource(stream));
    }
}
