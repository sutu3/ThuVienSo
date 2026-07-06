package org.example.thuvienso.Mapper;


import org.example.thuvienso.Dto.Request.AccountRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Module.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "roleEntity",ignore = true)
    AccountEntity toEntity(AccountRequest request);

    //    @Mapping(target = "role",ignore = true)
    AccountResponse toResponse(AccountEntity accountEntity);
}
