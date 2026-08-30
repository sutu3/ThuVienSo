package org.example.thuvienso.Service.Impl;

import io.minio.*;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Dto.Response.File.FileResponseNoList;
import org.example.thuvienso.Dto.Response.FileUploadResponse;
import org.example.thuvienso.Enum.BookFileRole;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Enum.TypeFile;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Helper.FileResponseHelper;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.LocalStorage;
import org.example.thuvienso.Helper.ThumbnailGenerator;
import org.example.thuvienso.Mapper.FileMapper;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.CategoryEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Repo.CategoryRepo;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.CategoryService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {
    private final CategoryRepo categoryRepo;


    private final DocumentRepo documentRepo;
    DocumentService documentService;
    FileRepo fileRepo;
    MinioService minioService;
    FileMapper fileMapper;
    MinioClient minioClient;
    ThumbnailGenerator thumbnailGenerator;
    LocalStorage localStorage;
    GetUrl getUrl;
    CategoryService categoryService;
    FileResponseHelper fileServiceHelper;

    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file, String idDocument) throws Exception {
        if (file.getSize() > 100L * 1024 * 1024) throw new AppException(ErrorCode.FILE_IS_TO_BIG);
        if(fileRepo.existsByDocumentEntity_IdDocumentAndFileNameAndTypeFile(idDocument,file.getOriginalFilename(),TypeFile.fromMimeType(file.getContentType()))) throw new AppException(ErrorCode.FILE_IS_EXIST);
        FileUploadResponse uploaded = minioService.upload(file);
        DocumentEntity document = documentService.getById(idDocument);

        // Toàn bộ logic thumbnail nằm trong helper
        String thumbObject = thumbnailGenerator.applyThumbnail(file, document);

        FileEntity fileEntity = FileEntity.builder()
                .partFile(uploaded.getObjectName())
                .fileName(uploaded.getOriginalFileName())
                .typeFile(TypeFile.fromMimeType(uploaded.getContentType()))
                .thumbnail(thumbObject)
                .size(uploaded.getSize())
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
                .map(file -> {
                    FileResponse response = fileMapper.toResponse(file);
                    // gán vai trò dựa vào entity (còn documentEntity)
                    response.setBookFile(resolveBookRole(file));
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
                                        + file.getFileName(),
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
    private BookFileRole resolveBookRole(FileEntity file) {
        DocumentEntity doc = file.getDocumentEntity();
        // chỉ phân loại khi document là BOOK
        if (doc == null || doc.getTypeDocument() != TypeDocument.BOOK) {
            return BookFileRole.KHÁC;
        }
        TypeFile type = file.getTypeFile();
        if (type == null) return BookFileRole.KHÁC;

        return switch (type) {
            case PNG, JPG      -> BookFileRole.THUMBNAIL;   // ảnh -> ảnh bìa
            case PDF           -> BookFileRole.Sách_Số;       // pdf -> sách số
            case MP3           -> BookFileRole.Sách_Nói;  // audio -> sách nói
            default            -> BookFileRole.KHÁC;       // mp4/docx/zip...
        };
    }
    @Override
    @Transactional
    public List<FileResponse> uploadFilesToFolder(MultipartFile[] files, String idFolder) throws Exception {
        if (files == null || files.length == 0) throw new AppException(ErrorCode.FILE_NOT_FOUND);

        // 1. Folder đã có document "chứa file" chưa? Có -> tái dùng, chưa -> tạo mới
        String idDocument = documentRepo
                .findFirstByFolderEntity_IdFolderAndTypeDocumentAndIsDeletedFalse(idFolder, TypeDocument.DOCUMENT)
                .map(DocumentEntity::getIdDocument)
                .orElseGet(() -> {
                    CategoryEntity category = categoryRepo.findByCategoryName("Folder")
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                    return documentService.create(DocumentRequest.builder()
                            .status(StatusDocument.Approve.name())
                            .typeDocument(TypeDocument.DOCUMENT.name())
                            .thumbnail("")
                            .categoryEntity(category.getIdCategory())
                            .folderEntity(idFolder)
                            .title("Document chứa file trong Folder")
                            .build()).getIdDocument();
                });

        // 2. Upload lần lượt từng file vào document đó (mỗi file tự suy typeFile + thumbnail)
        List<FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                responses.add(uploadFile(file, idDocument));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public List<FileResponse> getAllFileByFolder(String idFolder) {
        List<DocumentEntity> documentEntities = documentRepo.findByFolderEntity_IdFolder(idFolder);

        return documentEntities.stream()
                .map(DocumentEntity::getFileEntity)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(file -> {
                    FileResponse response = fileMapper.toResponse(file);
                    fileServiceHelper.buildUrl(response);
                    return response;
                })
                .toList();
    }

    @Override
    public FileEntity getById(String idFile) {
        return fileRepo.findById(idFile)
                .orElseThrow(()->new AppException(ErrorCode.FILE_NOT_FOUND));
    }

    @Override
    @Transactional
    public List<FileResponse> copyFile(List<String> files, String idFolderParent) {
        // 1. Tìm document "chứa file" trong folder đích; chưa có thì tạo mới
        String idDocument = documentRepo
                .findFirstByFolderEntity_IdFolderAndTypeDocumentAndIsDeletedFalse(idFolderParent, TypeDocument.DOCUMENT)
                .map(DocumentEntity::getIdDocument)
                .orElseGet(() -> {
                    CategoryEntity category = categoryRepo.findByCategoryName("Folder")
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                    return documentService.create(DocumentRequest.builder()
                            .status(StatusDocument.Approve.name())
                            .typeDocument(TypeDocument.DOCUMENT.name())
                            .thumbnail("")
                            .categoryEntity(category.getIdCategory())
                            .folderEntity(idFolderParent)
                            .title("Document chứa file trong Folder")
                            .build()).getIdDocument();
                });

        // Lấy document đích 1 lần, dùng lại cho mọi file
        DocumentEntity targetDocument = documentService.getById(idDocument);


        // 2. Sao chép từng file (chỉ tham chiếu lại partFile/thumbnail, không copy bytes)
        List<FileResponse> responses = new ArrayList<>();
        for (String fileId : files) {
            FileEntity source = fileRepo.findById(fileId)
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));


                if (Boolean.TRUE.equals(source.getIsDeleted())) continue; // bỏ file đã xóa mềm

            if(fileRepo.existsByDocumentEntity_IdDocumentAndFileNameAndTypeFile(targetDocument.getIdDocument(),source.getFileName(),source.getTypeFile())){
                throw new AppException(ErrorCode.FILE_IS_EXIST);
            }

            FileEntity newFile = FileEntity.builder()
                    .fileName(source.getFileName())
                    .partFile(source.getPartFile())
                    .typeFile(source.getTypeFile())
                    .thumbnail(source.getThumbnail())
                    .documentEntity(targetDocument)
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();

            FileResponse response = fileMapper.toResponse(fileRepo.save(newFile));
            response.setBookFile(resolveBookRole(newFile));
            fileServiceHelper.buildUrl(response);
            responses.add(response);
        }
        return responses;
    }
    @Override
    @Transactional
    public List<FileResponse> cutFile(List<String> files, String idFolderParent) {
        // 1. Tìm document "chứa file" trong folder đích; chưa có thì tạo mới
        String idDocument = documentRepo
                .findFirstByFolderEntity_IdFolderAndTypeDocumentAndIsDeletedFalse(idFolderParent, TypeDocument.DOCUMENT)
                .map(DocumentEntity::getIdDocument)
                .orElseGet(() -> {
                    CategoryEntity category = categoryRepo.findByCategoryName("Folder")
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                    return documentService.create(DocumentRequest.builder()
                            .status(StatusDocument.Approve.name())
                            .typeDocument(TypeDocument.DOCUMENT.name())
                            .thumbnail("")
                            .categoryEntity(category.getIdCategory())
                            .folderEntity(idFolderParent)
                            .title("Document chứa file trong Folder")
                            .build()).getIdDocument();
                });

        // Lấy document đích 1 lần, dùng lại cho mọi file
        DocumentEntity targetDocument = documentService.getById(idDocument);

        // 2. Chuyển từng file (chỉ tham chiếu lại partFile/thumbnail, không cut bytes)
        List<FileResponse> responses = new ArrayList<>();
        for (String fileId : files) {
            FileEntity source = fileRepo.findById(fileId)
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

            if (Boolean.TRUE.equals(source.getIsDeleted())) continue; // bỏ file đã xóa mềm
            if(fileRepo.existsByDocumentEntity_IdDocumentAndFileNameAndTypeFile(targetDocument.getIdDocument(),source.getFileName(),source.getTypeFile())){
                throw new AppException(ErrorCode.FILE_IS_EXIST);
            }

           source.setDocumentEntity(targetDocument);

            FileResponse response = fileMapper.toResponse(fileRepo.save(source));
            response.setBookFile(resolveBookRole(source));
            try {
                if (response.getPartFile() != null && !response.getPartFile().isBlank())
                    response.setPartFile(getUrl.getFileUrl(response.getPartFile()));
                if (response.getThumbnail() != null && !response.getThumbnail().isBlank())
                    response.setThumbnail(getUrl.getFileUrl(response.getThumbnail()));
            } catch (Exception e) {
                throw new RuntimeException("Không thể tạo URL cho file: " + source.getFileName(), e);
            }
            responses.add(response);
        }
        return responses;
    }
    @Override
    @Transactional
    public List<FileResponse> uploadFilesToCategory(MultipartFile[] files, String idCategory) throws Exception {
        if (files == null || files.length == 0) throw new AppException(ErrorCode.FILE_NOT_FOUND);

        // xác nhận category tồn tại (ném lỗi rõ ràng nếu id sai)
        CategoryEntity category = categoryService.getById(idCategory);

        List<FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            // 1. Suy typeFile -> typeDocument (mp4 -> VIDEO, mp3 -> AUDIO, ...)
            TypeFile typeFile = TypeFile.fromMimeType(file.getContentType());
            TypeDocument typeDocument = fileServiceHelper.mapFileToDocumentType(typeFile);

            // 2. Trong category này đã có document đúng loại chưa? Có -> tái dùng, chưa -> tạo mới
            String idDocument = documentRepo
                    .findFirstByCategoryEntity_IdCategoryAndTypeDocumentAndIsDeletedFalse(idCategory, typeDocument)
                    .map(DocumentEntity::getIdDocument)
                    .orElseGet(() -> documentService.create(DocumentRequest.builder()
                            .status(StatusDocument.Approve.name())
                            .typeDocument(typeDocument.name())
                            .thumbnail("")
                            .categoryEntity(category.getIdCategory())
                            .title("Tài liệu " + typeDocument.name() + " - " + category.getCategoryName())
                            .build()).getIdDocument());

            // 3. Upload file vào document đó (tự lo thumbnail + kiểm tra trùng)
            responses.add(uploadFile(file, idDocument));
        }
        return responses;
    }
    @Override
    @Transactional
    public List<FileResponse> getAllFileByCategory(String idCategory) {
        List<DocumentEntity> documentEntities =
                documentRepo.findByCategoryEntity_IdCategoryAndIsDeletedFalse(idCategory);

        return documentEntities.stream()
                .map(DocumentEntity::getFileEntity)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(file -> {
                    FileResponse response = fileMapper.toResponse(file);
                    try {
                        if (response.getPartFile() != null && !response.getPartFile().isBlank())
                            response.setPartFile(getUrl.getFileUrl(response.getPartFile()));
                        if (response.getThumbnail() != null && !response.getThumbnail().isBlank())
                            response.setThumbnail(getUrl.getFileUrl(response.getThumbnail()));
                    } catch (Exception e) {
                        throw new RuntimeException("Không thể tạo URL cho file: " + file.getFileName(), e);
                    }
                    return response;
                })
                .toList();
    }


}


