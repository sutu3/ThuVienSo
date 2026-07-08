package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.AccountRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.AccountMapper;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.RoleEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.RoleRepo;
import org.example.thuvienso.Service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountServiceImpl implements AccountService {
    private final RoleRepo roleRepo;
    AccountMapper accountMapper;
    AccountRepo accountRepo;
    @Override
    public AccountResponse createAccount(AccountRequest request) {
        AccountEntity entity=accountMapper.toEntity(request);
        RoleEntity role=roleRepo.findByRoleName(request.getRole())
                .orElseThrow(()->new AppException(ErrorCode.ROLE_NOT_FOUND));
        entity.setRoleEntity(role);
        entity.setIsDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        return accountMapper.toResponse(accountRepo.save(entity));
    }

    @Override
    public AccountEntity getAccountById(String idAccount) {
        AccountEntity account=accountRepo.findById(idAccount)
                .orElseThrow(()->new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        return account;
    }

    @Override
    public AccountResponse getAccounResponsetById(String idAccount) {
        return accountMapper.toResponse(getAccountById(idAccount));
    }

    @Override
    public List<AccountResponse> getAllAccount() {
        return accountRepo.findAll().stream()
                .filter(accountEntity -> !accountEntity.getIsDeleted())
                .map(accountMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void lockAccount(String id) {
        AccountEntity account=getAccountById(id);
        account.setIsDeleted(false);
        account.setDeletedAt(LocalDateTime.now());
        accountRepo.save(account);
    }
}
