package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.BookMapper;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.BookEntity;
import org.example.thuvienso.Module.ReadingHistoryEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.ReadingHistoryRepo;
import org.example.thuvienso.Service.BookService;
import org.example.thuvienso.Service.ReadingHistoryService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    ReadingHistoryRepo readingHistoryRepo;
    AccountRepo accountRepo;
    BookService bookService;
    BookMapper bookMapper;

    @Override
    @Transactional
    public void markRead(String idBook) {
        AccountEntity account = getCurrentAccount();
        BookEntity book = bookService.getById(idBook); // ném BOOK_NOT_FOUND nếu không có

        ReadingHistoryEntity record = readingHistoryRepo
                .findByAccount_IdAccountAndBook_IdBook(account.getIdAccount(), idBook)
                .orElse(null);

        if (record == null) {
            record = ReadingHistoryEntity.builder()
                    .account(account)
                    .book(book)
                    .lastReadAt(LocalDateTime.now())
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            record.setLastReadAt(LocalDateTime.now()); // hướng 2: chỉ cập nhật thời gian
        }
        readingHistoryRepo.save(record);
    }

    @Override
    public List<BookResponse> getMyHistory() {
        AccountEntity account = getCurrentAccount();
        return readingHistoryRepo
                .findByAccount_IdAccountOrderByLastReadAtDesc(account.getIdAccount())
                .stream()
                .map(h -> bookMapper.toResponse(h.getBook()))
                .collect(Collectors.toList());
    }

    private AccountEntity getCurrentAccount() {
        String userName = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return accountRepo.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}