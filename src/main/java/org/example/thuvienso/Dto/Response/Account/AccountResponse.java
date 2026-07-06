package org.example.thuvienso.Dto.Response.Account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Dto.Response.Role.RoleResponseNoList;
import org.example.thuvienso.Module.RoleEntity;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    String idAccount;

    String accountName;

    String password;

    String userName;

    RoleResponseNoList roleEntity;

}
