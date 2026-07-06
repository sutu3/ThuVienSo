package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Request.AccountRequest;
import org.example.thuvienso.Dto.Request.RoleRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Dto.Response.Role.RoleResponse;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "account",ignore = true)
    RoleEntity toEntity(RoleRequest request);

    //    @Mapping(target = "role",ignore = true)
    RoleResponse toResponse(RoleEntity Entity);
}
