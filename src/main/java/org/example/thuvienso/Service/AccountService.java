package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.AccountRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Module.AccountEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountEntity getAccountById(String idAccount);
    AccountResponse getAccounResponsetById(String idAccount);
    List<AccountResponse> getAllAccount();

}
