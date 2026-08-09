package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.AuditAction;

@Entity
@Table(name = "audit_log")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idAuditLog", columnDefinition = "VARCHAR(36) COMMENT 'Id nhật ký'")
    String idAuditLog;

    @Column(name = "idAccount", columnDefinition = "VARCHAR(36) COMMENT 'Id tài khoản thao tác'")
    String idAccount;

    @Column(name = "userName", columnDefinition = "VARCHAR(256) COMMENT 'Tên đăng nhập thao tác'")
    String userName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'Loại hành động'")
    AuditAction action;

    @Column(name = "targetType", columnDefinition = "VARCHAR(100) COMMENT 'Đối tượng bị tác động (Document, Book...)'")
    String targetType;

    @Column(name = "httpMethod", columnDefinition = "VARCHAR(10) COMMENT 'HTTP method'")
    String httpMethod;

    @Column(name = "uri", columnDefinition = "VARCHAR(512) COMMENT 'Đường dẫn request'")
    String uri;

    @Column(name = "ipAddress", columnDefinition = "VARCHAR(64) COMMENT 'IP người dùng'")
    String ipAddress;

    @Column(name = "success", columnDefinition = "BOOL COMMENT 'Thành công hay thất bại'")
    Boolean success;

    @Column(name = "detail", columnDefinition = "TEXT COMMENT 'Chi tiết / thông báo lỗi'")
    String detail;
}