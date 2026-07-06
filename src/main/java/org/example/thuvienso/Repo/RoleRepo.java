package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.RoleEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<RoleEntity,String>, JpaSpecificationExecutor<RoleEntity> {
    Optional<RoleEntity> findByRoleName(String roleName);
}
