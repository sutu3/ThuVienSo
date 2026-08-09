package org.example.thuvienso.Mapper;

import org.example.thuvienso.Dto.Response.AuditLog.AuditLogResponse;
import org.example.thuvienso.Module.AuditLogEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLogEntity entity);
}