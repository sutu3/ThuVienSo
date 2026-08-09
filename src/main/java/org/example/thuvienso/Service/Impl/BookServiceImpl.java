package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.BookRequest;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Form.BookForm;
import org.example.thuvienso.Mapper.BookMapper;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.CategoryEntity;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.BookRepo;
import org.example.thuvienso.Service.BookService;
import org.example.thuvienso.Service.CategoryService;
import org.example.thuvienso.Service.DocumentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookServiceImpl implements BookService {
    BookRepo bookRepo;
    BookMapper bookMapper;
    CategoryService categoryService;
    DocumentService documentService;

    @Override
    public BookResponse createBook(BookRequest request) {
        if (request.getBookCode() != null && bookRepo.existsByBookCode(request.getBookCode())) {
            throw new AppException(ErrorCode.BOOK_IS_EXIST);
        }
        BookEntity book = bookMapper.toEntity(request);

        if (request.getCategoryEntity() != null && !request.getCategoryEntity().isBlank()) {
            CategoryEntity category = categoryService.getById(request.getCategoryEntity());
            book.setCategoryEntity(category);
        }
        if (request.getDocumentEntity() != null && !request.getDocumentEntity().isBlank()) {
            DocumentEntity document = documentService.getById(request.getDocumentEntity());
            book.setDocumentEntity(document);
        }

        int total = request.getTotalCopies() == null ? 0 : request.getTotalCopies();
        book.setTotalCopies(total);
        book.setAvailableCopies(total); // ban đầu số bản còn = tổng số bản
        book.setIsDeleted(false);
        book.setCreatedAt(LocalDateTime.now());
        bookRepo.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse getByIdResponse(String id) {
        return bookMapper.toResponse(getById(id));
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
}