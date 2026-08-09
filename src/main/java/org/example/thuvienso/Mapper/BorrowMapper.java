package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Response.Borrow.BorrowResponse;
import org.example.thuvienso.Module.BorrowRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowMapper {
    @Mapping(target = "idBook", source = "book.idBook")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "idAccount", source = "account.idAccount")
    @Mapping(target = "borrowerName", source = "account.userName")
    BorrowResponse toResponse(BorrowRecordEntity entity);
}