package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepo
        extends JpaRepository<AuditLogEntity, String>, JpaSpecificationExecutor<AuditLogEntity> {

    Page<AuditLogEntity> findAllByUserName(String userName, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE', a.createdAt), COUNT(a) FROM AuditLogEntity a WHERE a.isDeleted = false AND a.createdAt >= :from GROUP BY FUNCTION('DATE', a.createdAt) ORDER BY FUNCTION('DATE', a.createdAt)")
    java.util.List<Object[]> countActivityByDay(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from);
}
