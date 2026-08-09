package org.example.thuvienso.Service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Response.AuditLog.AuditLogResponse;
import org.example.thuvienso.Enum.AuditAction;
import org.example.thuvienso.Mapper.AuditLogMapper;
import org.example.thuvienso.Module.AuditLogEntity;
import org.example.thuvienso.Repo.AuditLogRepo;
import org.example.thuvienso.Service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogServiceImpl implements AuditLogService {

    AuditLogRepo auditLogRepo;
    AuditLogMapper auditLogMapper;

    @Override
    public void record(AuditAction action, String targetType, String httpMethod,
                       String uri, boolean success, String detail) {
        try {
            String idAccount = null;
            String userName = "anonymous";

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.getPrincipal() instanceof Jwt jwt) {
                idAccount = jwt.getClaim("sub");
                Object nameClaim = jwt.getClaim("userName");
                if (nameClaim != null) userName = nameClaim.toString();
            }

            AuditLogEntity entity = AuditLogEntity.builder()
                    .idAccount(idAccount)
                    .userName(userName)
                    .action(action)
                    .targetType(targetType)
                    .httpMethod(httpMethod)
                    .uri(uri)
                    .ipAddress(currentIp())
                    .success(success)
                    .detail(detail)
                    .isDeleted(false)
                    .build();

            auditLogRepo.save(entity);
        } catch (Exception e) {
            // Không để việc ghi log làm hỏng nghiệp vụ chính
            log.warn("Cannot write audit log: {}", e.getMessage());
        }
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            return (ip == null || ip.isBlank()) ? req.getRemoteAddr() : ip.split(",")[0];
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepo.findAll(pageable).map(auditLogMapper::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getByUserName(String userName, Pageable pageable) {
        return auditLogRepo.findAllByUserName(userName, pageable)
                .map(auditLogMapper::toResponse);
    }
}