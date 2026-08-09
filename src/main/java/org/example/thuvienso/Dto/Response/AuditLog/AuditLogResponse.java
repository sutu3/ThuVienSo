package org.example.thuvienso.Dto.Response.AuditLog;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Enum.AuditAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogResponse {
    String idAuditLog;
    String idAccount;
    String userName;
    AuditAction action;
    String targetType;
    String httpMethod;
    String uri;
    String ipAddress;
    Boolean success;
    String detail;
    LocalDateTime createdAt;
}