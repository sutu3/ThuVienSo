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
import org.example.thuvienso.Module.FavoriteEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.FavoriteRepo;
import org.example.thuvienso.Service.BookService;
import org.example.thuvienso.Service.FavoriteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteServiceImpl implements FavoriteService {

    FavoriteRepo favoriteRepo;
    AccountRepo accountRepo;
    BookService bookService;
    BookMapper bookMapper;

    @Override
    @Transactional
    public void addFavorite(String idBook) {
        AccountEntity account = getCurrentAccount();
        BookEntity book = bookService.getById(idBook);

        if (favoriteRepo.existsByAccount_IdAccountAndBook_IdBook(account.getIdAccount(), idBook)) {
            throw new AppException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        FavoriteEntity favorite = FavoriteEntity.builder()
                .account(account)
                .book(book)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepo.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(String idBook) {
        AccountEntity account = getCurrentAccount();
        FavoriteEntity favorite = favoriteRepo
                .findByAccount_IdAccountAndBook_IdBook(account.getIdAccount(), idBook)
                .orElseThrow(() -> new AppException(ErrorCode.FAVORITE_NOT_FOUND));
        favoriteRepo.delete(favorite);
    }

    @Override
    public List<BookResponse> getMyFavorites() {
        AccountEntity account = getCurrentAccount();
        return favoriteRepo
                .findByAccount_IdAccountOrderByCreatedAtDesc(account.getIdAccount())
                .stream()
                .map(f -> bookMapper.toResponse(f.getBook()))
                .collect(Collectors.toList());
    }

    private AccountEntity getCurrentAccount() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return accountRepo.findById(authentication.getToken().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}