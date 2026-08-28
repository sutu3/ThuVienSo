package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.ReadingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepo extends JpaRepository<ReadingHistoryEntity, String> {

    Optional<ReadingHistoryEntity> findByAccount_IdAccountAndBook_IdBook(String idAccount, String idBook);

    List<ReadingHistoryEntity> findByAccount_IdAccountOrderByLastReadAtDesc(String idAccount);
}