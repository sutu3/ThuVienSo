package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.BorrowRequest;
import org.example.thuvienso.Dto.Response.Borrow.BorrowResponse;
import org.example.thuvienso.Enum.BorrowStatus;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.BorrowMapper;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.BorrowRecordEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.BookRepo;
import org.example.thuvienso.Repo.BorrowRecordRepo;
import org.example.thuvienso.Service.BookService;
import org.example.thuvienso.Service.BorrowService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowServiceImpl implements BorrowService {
    private final BookRepo bookRepo;

    BorrowRecordRepo borrowRepo;
    BookService bookService;
    AccountRepo accountRepo;
    BorrowMapper borrowMapper;

    private static final int DEFAULT_BORROW_DAYS = 14;

    @Override
    @Transactional
    public BorrowResponse register(BorrowRequest request) {
        BookEntity book = bookService.getById(request.getIdBook());

        // còn bản in để mượn không
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }

        AccountEntity account = getCurrentAccount();
        int days = request.getBorrowDays() != null ? request.getBorrowDays() : DEFAULT_BORROW_DAYS;

        BorrowRecordEntity record = BorrowRecordEntity.builder()
                .book(book)
                .account(account)
                .status(BorrowStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(days))
                .note(request.getNote())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        return borrowMapper.toResponse(borrowRepo.save(record));
    }

    @Override
    @Transactional
    public BorrowResponse approve(String idBorrow) {
        BorrowRecordEntity record = getEntity(idBorrow);
        requireStatus(record, BorrowStatus.PENDING);

        BookEntity book = record.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }
        // giữ chỗ 1 bản
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepo.save(book);

        record.setStatus(BorrowStatus.APPROVED);
        record.setUpdatedAt(LocalDateTime.now());
        return borrowMapper.toResponse(borrowRepo.save(record));
    }

    @Override
    @Transactional
    public BorrowResponse reject(String idBorrow) {
        BorrowRecordEntity record = getEntity(idBorrow);
        requireStatus(record, BorrowStatus.PENDING);
        record.setStatus(BorrowStatus.REJECTED);
        record.setUpdatedAt(LocalDateTime.now());
        return borrowMapper.toResponse(borrowRepo.save(record));
    }

    @Override
    @Transactional
    public BorrowResponse markBorrowed(String idBorrow) {
        BorrowRecordEntity record = getEntity(idBorrow);
        requireStatus(record, BorrowStatus.APPROVED);
        record.setStatus(BorrowStatus.BORROWED);
        record.setBorrowDate(LocalDate.now());
        record.setUpdatedAt(LocalDateTime.now());
        return borrowMapper.toResponse(borrowRepo.save(record));
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(String idBorrow) {
        BorrowRecordEntity record = getEntity(idBorrow);
        if (record.getStatus() != BorrowStatus.BORROWED
                && record.getStatus() != BorrowStatus.OVERDUE) {
            throw new AppException(ErrorCode.BORROW_INVALID_STATUS);
        }
        // hoàn lại bản in
        BookEntity book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepo.save(book);

        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.now());
        record.setUpdatedAt(LocalDateTime.now());
        return borrowMapper.toResponse(borrowRepo.save(record));
    }

    @Override
    public List<BorrowResponse> getAll() {
        return borrowRepo.findAllByIsDeletedFalse().stream()
                .map(borrowMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getMyBorrows(String idAccount) {
        return borrowRepo.findByAccount_IdAccountAndIsDeletedFalse(idAccount).stream()
                .map(borrowMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowResponse getById(String idBorrow) {
        return borrowMapper.toResponse(getEntity(idBorrow));
    }

    // ===== helper =====
    private BorrowRecordEntity getEntity(String idBorrow) {
        return borrowRepo.findById(idBorrow)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_FOUND));
    }

    private void requireStatus(BorrowRecordEntity record, BorrowStatus expected) {
        if (record.getStatus() != expected) {
            throw new AppException(ErrorCode.BORROW_INVALID_STATUS);
        }
    }

    private AccountEntity getCurrentAccount() {
        String userName = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return accountRepo.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}