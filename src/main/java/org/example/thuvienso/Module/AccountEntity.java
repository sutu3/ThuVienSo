package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idAccount",columnDefinition = "VARCHAR(36) COMMENT 'Id của tài khoản'")
    String idAccount;

    @Column(name = "userName",columnDefinition = "VARCHAR(256) COMMENT 'tên đăng nhập'")
    String accountName;

    @Column(name = "password",columnDefinition = "VARCHAR(255) COMMENT 'mật khẩu đăng nhập'")
    String password;

    @Column(name = "name",columnDefinition = "VARCHAR(255) COMMENT 'tên người dùng'")
    String userName;
    @ManyToOne
    @JoinColumn(name = "idRole",nullable = false)
    RoleEntity roleEntity;

}
