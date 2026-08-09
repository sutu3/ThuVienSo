package org.example.thuvienso.Config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.thuvienso.Enum.AuditAction;
import org.example.thuvienso.Service.AuditLogService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    // Áp dụng cho tất cả class trong package Controller
    @Pointcut("within(org.example.thuvienso.Controller..*)")
    public void controllerLayer() {}

    @Around("controllerLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest req = currentRequest();
        String method = req == null ? "" : req.getMethod();

        // Chỉ ghi log thao tác ghi dữ liệu
        boolean isWrite = method.equals("POST")
                || method.equals("PUT")
                || method.equals("DELETE");

        if (!isWrite) {
            return joinPoint.proceed();
        }

        String uri = req.getRequestURI();
        String targetType = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            auditLogService.record(actionOf(method), targetType, method, uri, true, null);
            return result;
        } catch (Throwable ex) {
            auditLogService.record(actionOf(method), targetType, method, uri,
                    false, ex.getMessage());
            throw ex;
        }
    }

    private AuditAction actionOf(String method) {
        return switch (method) {
            case "POST" -> AuditAction.CREATE;
            case "PUT" -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            default -> AuditAction.OTHER;
        };
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}