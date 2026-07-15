package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentRepo extends JpaRepository<DocumentEntity,String>, JpaSpecificationExecutor<DocumentEntity> {
    List<DocumentEntity> findByFolderEntity_IdFolder(String idFolder);
    List<DocumentEntity> findAllByIsDeleted(Boolean isDeleted);
    List<DocumentEntity> findAllByTitle(String title);
    Page<DocumentEntity> findAllByIsDeleted(boolean isDeleted, Pageable pageable);
    Page<DocumentEntity> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Xem nhiều nhất
    Page<DocumentEntity> findByIsDeletedFalseOrderByViewCountDesc(Pageable pageable);

    // Liên quan theo category, loại trừ chính nó
    Page<DocumentEntity> findByCategoryEntity_IdCategoryAndIdDocumentNotAndIsDeletedFalse(
            String idCategory, String idDocument, Pageable pageable);
}
