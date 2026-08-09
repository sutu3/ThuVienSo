package org.example.thuvienso.Controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.AuditLog.AuditLogResponse;
import org.example.thuvienso.Service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-log")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogController {

    AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<Page<AuditLogResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.<Page<AuditLogResponse>>builder()
                .code(0)
                .success(true)
                .message("Lấy nhật ký hệ thống thành công")
                .Result(auditLogService.getAll(pageable))
                .build();
    }

    @GetMapping("/user/{userName}")
    public ApiResponse<Page<AuditLogResponse>> getByUser(
            @PathVariable("userName") String userName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.<Page<AuditLogResponse>>builder()
                .code(0)
                .success(true)
                .message("Lấy nhật ký theo người dùng thành công")
                .Result(auditLogService.getByUserName(userName, pageable))
                .build();
    }
}