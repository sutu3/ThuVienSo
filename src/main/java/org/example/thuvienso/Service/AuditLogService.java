package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Response.AuditLog.AuditLogResponse;
import org.example.thuvienso.Enum.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void record(AuditAction action, String targetType, String httpMethod,
                String uri, boolean success, String detail);

    Page<AuditLogResponse> getAll(Pageable pageable);

    Page<AuditLogResponse> getByUserName(String userName, Pageable pageable);
}