package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.BookRequest;
import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.File.FileResponse;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Form.BookForm;
import org.example.thuvienso.Helper.GetUrl;
import org.example.thuvienso.Helper.QrCodeGenerator;
import org.example.thuvienso.Mapper.BookMapper;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.CategoryEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Repo.BookRepo;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Service.BookService;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.DocumentService;
import org.example.thuvienso.Service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookServiceImpl implements BookService {
    private final DocumentRepo documentRepo;
    BookRepo bookRepo;
    BookMapper bookMapper;
    CategoryService categoryService;
    DocumentService documentService;
    FileService fileService;
    GetUrl getUrl;
    QrCodeGenerator qrCodeGenerator;

    @Override
    public BookResponse createBook(BookRequest request,MultipartFile file,MultipartFile thumbnailBook) throws Exception {
        if (request.getBookCode() != null && bookRepo.existsByBookCode(request.getBookCode())) {
            throw new AppException(ErrorCode.BOOK_IS_EXIST);
        }
        BookEntity book = bookMapper.toEntity(request);

        CategoryEntity category = categoryService.getById(request.getCategoryEntity());
        book.setCategoryEntity(category);

        DocumentResponse documentResponse=documentService.create(DocumentRequest.builder()
                .status(StatusDocument.Approve.name())
                .typeDocument(TypeDocument.BOOK.name())
                .thumbnail("")
                .categoryEntity(category.getIdCategory())
                .title("Dữ liệu sách: "+request.getTitle())
                .build());
        fileService.uploadFile(file,documentResponse.getIdDocument());
        if (thumbnailBook != null && !thumbnailBook.isEmpty()) {
            fileService.uploadFile(thumbnailBook, documentResponse.getIdDocument());
        }

        int total = request.getTotalCopies() == null ? 0 : request.getTotalCopies();
        book.setTotalCopies(total);
        book.setDocumentEntity(documentService.getById(documentResponse.getIdDocument()));
        book.setAvailableCopies(total); // ban đầu số bản còn = tổng số bản
        book.setIsDeleted(false);
        book.setCreatedAt(LocalDateTime.now());
        bookRepo.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse getByIdResponse(String id) {
        BookResponse res = bookMapper.toResponse(getById(id));
        if (res.getThumbnail() != null && !res.getThumbnail().isBlank())
            res.setThumbnail(getUrl.getFileUrl(res.getThumbnail()));
        res.setQrCode(getUrl.getFileUrl(qrCodeGenerator.generate(res.getIdBook())));
        return res;
    }

    @Override
    public BookEntity getById(String id) {
        return bookRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
    }

    @Override
    public BookResponse getByBookCode(String bookCode) {
        BookEntity book = bookRepo.findByBookCode(bookCode)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        return bookMapper.toResponse(book);
    }

    @Override
    public List<BookResponse> getAll() {
        return bookRepo.findAll().stream()
                .filter(book -> !book.getIsDeleted())
                .map(bookMapper::toResponse)
                .peek(res -> {
                    try {
                        if (res.getThumbnail() != null && !res.getThumbnail().isBlank())
                            res.setThumbnail(getUrl.getFileUrl(res.getThumbnail()));
                        String qrObject = qrCodeGenerator.generate(res.getIdBook());
                        res.setQrCode(getUrl.getFileUrl(qrObject));
                    } catch (Exception e) {
                        log.warn("Cannot build thumbnail url", e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deletedById(String id) {
        BookEntity book = getById(id);
        book.setIsDeleted(true);
        book.setDeletedAt(LocalDateTime.now());
        bookRepo.save(book);
    }

    @Override
    public BookResponse updateBook(BookForm form, String id) {
        BookEntity book = getById(id);
        Integer oldTotal = book.getTotalCopies();
        bookMapper.update(book, form);
        // Điều chỉnh availableCopies theo thay đổi tổng số bản (giữ số bản đang được mượn)
        if (form.getTotalCopies() != null && oldTotal != null) {
            int borrowed = oldTotal - (book.getAvailableCopies() == null ? 0 : book.getAvailableCopies());
            book.setAvailableCopies(Math.max(0, form.getTotalCopies() - borrowed));
        }
        book.setUpdatedAt(LocalDateTime.now());
        bookRepo.save(book);
        return bookMapper.toResponse(book);
    }
    @Override
    public FileResponse addAudio(String idBook, MultipartFile audio) throws Exception {
        // 1) Lấy sách + kiểm tra tồn tại
        BookEntity book = getById(idBook);

        // 2) Sách phải có document liên kết (nơi chứa các file)
        if (book.getDocumentEntity() == null) {
            throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
        }

        // 3) Chỉ cho phép file audio
        String contentType = audio.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new AppException(ErrorCode.FILE_NOT_SUPPORTED);
        }

        // 4) Upload vào đúng document của sách
        return fileService.uploadFile(audio, book.getDocumentEntity().getIdDocument());
    }
}