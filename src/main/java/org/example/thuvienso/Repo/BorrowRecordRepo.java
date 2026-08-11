package org.example.thuvienso.Repo;

import org.example.thuvienso.Enum.BorrowStatus;
import org.example.thuvienso.Module.BorrowRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepo extends JpaRepository<BorrowRecordEntity, String>,
        JpaSpecificationExecutor<BorrowRecordEntity> {

    List<BorrowRecordEntity> findAllByIsDeletedFalse();

    List<BorrowRecordEntity> findByAccount_IdAccountAndIsDeletedFalse(String idAccount);

    List<BorrowRecordEntity> findByStatusAndIsDeletedFalse(BorrowStatus status);

    int countByStatus(BorrowStatus status);

    // phục vụ job tính quá hạn  
    List<BorrowRecordEntity> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);
}