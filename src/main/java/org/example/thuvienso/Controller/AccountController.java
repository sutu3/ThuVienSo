package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.AccountRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    AccountService accountService;
    /**
     * GET ACCOUNT INFOR BY TOKEN
     */

    @GetMapping()
    public ApiResponse<AccountResponse> getAccountByToken(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String scope = jwt.getClaim("sub");
        return ApiResponse.<AccountResponse>builder()
                .code(0)
                .Result(accountService.getAccounResponsetById(scope))
                .message("Successfully")
                .success(true)
                .build();
    }
    /**
     * CREATE ACCOUNT
     */
    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@RequestBody @Valid AccountRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .code(0)
                .message("Tạo tài khoản thành công")
                .success(true)
                .Result(accountService.createAccount(request))
                .build();
    }

    /**
     * GET ACCOUNT ENTITY BY ID
     */
    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable("id") String id) {
        return ApiResponse.<AccountResponse>builder()
                .code(0)
                .message("Lấy thông tin tài khoản thành công")
                .success(true)
                .Result(accountService.getAccounResponsetById(id))
                .build();
    }

    /**
     * GET ALL ACCOUNTS
     */
    @GetMapping("/getAll")
    public ApiResponse<List<AccountResponse>> getAllAccounts() {
        return ApiResponse.<List<AccountResponse>>builder()
                .code(0)
                .message("Lấy danh sách thông tin tài khoản thành công")
                .success(true)
                .Result(accountService.getAllAccount())
                .build();
    }
    /**
     * GET ALL ACCOUNTS
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> getAllAccounts(@PathVariable("id") String id) {
        accountService.lockAccount(id);
        return ApiResponse.<String>builder()
                .code(0)
                .message("Khóa tài khoản thành công")
                .success(true)
                .Result("Success")
                .build();
    }
}
