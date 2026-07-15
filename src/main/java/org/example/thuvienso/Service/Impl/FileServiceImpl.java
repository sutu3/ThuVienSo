package org.example.thuvienso.Service.Impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
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
import org.example.thuvienso.Mapper.FileMapper;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.DocumentService;
import org.example.thuvienso.Service.FileService;
import org.example.thuvienso.Service.MinioService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file,String idDocument) throws Exception {

        if(file.getSize() > 50) throw new AppException(ErrorCode.FILE_IS_TO_BIG);
        FileUploadResponse fileUploadResponse = minioService.upload(file);

        FileEntity fileEntity = FileEntity.builder()
                .partFile(fileUploadResponse.getObjectName())
                .fileName(fileUploadResponse.getOriginalFileName())
                .typeFile(
                        TypeFile.fromMimeType(
                                fileUploadResponse.getContentType()
                        )
                )
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        DocumentEntity document=documentService.getById(idDocument);
        fileEntity.setDocumentEntity(document);

        return fileMapper.toResponse(fileRepo.save(fileEntity));
    }

    @Override
    public ResponseEntity<InputStreamResource> viewFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        FileEntity fileEntity=fileRepo.findById(id).orElseThrow(()->new AppException(ErrorCode.FILE_NOT_FOUND));
        InputStream stream =
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket("thuvienso")
                                .object(fileEntity.getPartFile())
                                .build()
                );
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                fileEntity.getTypeFile().getMimeType()
                        )
                )
                .body(
                        new InputStreamResource(stream)
                );
    }

    @Override
    public ResponseEntity<InputStreamResource> dowloadFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        FileEntity fileEntity=fileRepo.findById(id).orElseThrow(()->new AppException(ErrorCode.FILE_NOT_FOUND));
        InputStream stream =
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket("thuvienso")
                                .object(fileEntity.getPartFile())
                                .build()
                );
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                fileEntity.getTypeFile().getMimeType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileEntity.getPartFile()
                )
                .body(
                        new InputStreamResource(stream)
                );
    }

    @Override
    public FileEntity getByFileName(String fileName) {
        return fileRepo.findByFileName(fileName)
                .orElseThrow(()->new AppException(ErrorCode.FILE_NOT_FOUND));
    }

    @Override
    public List<FileResponse> getByIdDocument(String idDocument) {
        return fileRepo.findByDocumentEntity_IdDocument(idDocument)
                .stream()
                .map(fileMapper::toResponse)
                .toList();
    }
    @Override
    public void deleteFile(String id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        FileEntity fileEntity = fileRepo.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.FILE_NOT_FOUND));

        // xóa object trong MinIO
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("thuvienso")
                        .object(fileEntity.getPartFile())
                        .build()
        );

        // xóa DB
        fileRepo.delete(fileEntity);

        // hoặc soft delete
//    fileEntity.setIsDeleted(true);
//    fileRepo.save(fileEntity);
    }
}
