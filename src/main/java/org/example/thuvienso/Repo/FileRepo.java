package org.example.thuvienso.Repo;

import org.example.thuvienso.Enum.TypeFile;
import org.example.thuvienso.Module.FileEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepo extends JpaRepository<FileEntity, String> {
    Optional<FileEntity> findByFileName(String fileName);

    List<FileEntity> findByDocumentEntity_IdDocument(String idDocument);

    List<FileEntity> findAllByDocumentEntity_IdDocument(String idDocument);

    boolean existsByDocumentEntity_IdDocumentAndFileNameAndTypeFile(String idDocument, String fileName, TypeFile typeFile);

    List<FileEntity> findByDocumentEntityIsNullAndCreatedAtBefore(LocalDateTime threshold);

    long countByIsDeletedFalse();
}
