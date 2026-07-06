package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.AccountEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<AccountEntity,String>, JpaSpecificationExecutor<AccountEntity> {
    Optional<AccountEntity> findByUserName(String userName);
}
