package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idRole",columnDefinition = "VARCHAR(36) COMMENT 'Id của vai trò'")
    String idRole;

    @Column(name = "roleName",columnDefinition = "VARCHAR(256) COMMENT 'tên vai trò'")
    String roleName;

    @OneToMany(mappedBy="roleEntity")
    List<AccountEntity> account;
}
