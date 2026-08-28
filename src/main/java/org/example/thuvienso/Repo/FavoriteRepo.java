package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepo extends JpaRepository<FavoriteEntity, String> {

    boolean existsByAccount_IdAccountAndBook_IdBook(String idAccount, String idBook);

    Optional<FavoriteEntity> findByAccount_IdAccountAndBook_IdBook(String idAccount, String idBook);

    List<FavoriteEntity> findByAccount_IdAccountOrderByCreatedAtDesc(String idAccount);
}