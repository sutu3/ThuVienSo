package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepo extends JpaRepository<FileEntity,String> {
    Optional<FileEntity> findByFileName(String fileName);
    Optional<FileEntity> findByDocumentEntity_IdDocument(String idDocument);
    List<FileEntity> findByDocumentEntityIsNullAndCreatedAtBefore(LocalDateTime threshold);
}
