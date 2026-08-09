package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepo extends JpaRepository<BookEntity, String>, JpaSpecificationExecutor<BookEntity> {

    Optional<BookEntity> findByBookCode(String bookCode);

    boolean existsByBookCode(String bookCode);

    int countByIsDeletedFalse();

    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM BookEntity b WHERE b.isDeleted = false")
    int sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM BookEntity b WHERE b.isDeleted = false")
    int sumAvailableCopies();
}