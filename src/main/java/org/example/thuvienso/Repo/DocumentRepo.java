package org.example.thuvienso.Repo;

import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DocumentRepo extends JpaRepository<DocumentEntity, String>, JpaSpecificationExecutor<DocumentEntity> {
    List<DocumentEntity> findByFolderEntity_IdFolder(String idFolder);

    List<DocumentEntity> findAllByIsDeleted(Boolean isDeleted);

    List<DocumentEntity> findAllByTitle(String title);

    Optional<DocumentEntity> findBySlugAndTypeDocumentAndIsDeletedFalse(String slug, TypeDocument typeDocument);

    boolean existsBySlugAndIdDocumentNot(String slug, String idDocument);

    boolean existsBySlug(String slug);

    Page<DocumentEntity> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Page<DocumentEntity> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Xem nhiều nhất
    Page<DocumentEntity> findByIsDeletedFalseOrderByViewCountDesc(Pageable pageable);

    // Liên quan theo category, loại trừ chính nó
    Page<DocumentEntity> findByCategoryEntity_IdCategoryAndIdDocumentNotAndIsDeletedFalse(
            String idCategory, String idDocument, Pageable pageable);

    long countByIsDeletedFalse();

    long countByIsDeletedFalseAndStatus(org.example.thuvienso.Enum.StatusDocument status);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(d.viewCount),0) FROM DocumentEntity d WHERE d.isDeleted = false")
    long sumViewCount();

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(d.downloadCount),0) FROM DocumentEntity d WHERE d.isDeleted = false")
    long sumDownloadCount();

    // Đếm theo loại tài liệu -> [TypeDocument, count]
    @org.springframework.data.jpa.repository.Query(
            "SELECT d.typeDocument, COUNT(d) FROM DocumentEntity d WHERE d.isDeleted = false GROUP BY d.typeDocument")
    java.util.List<Object[]> countGroupByType();

    @org.springframework.data.jpa.repository.Query("SELECT d.status, COUNT(d) FROM DocumentEntity d WHERE d.isDeleted = false GROUP BY d.status")
    java.util.List<Object[]> countGroupByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT d.categoryEntity.categoryName, COUNT(d) FROM DocumentEntity d WHERE d.isDeleted = false GROUP BY d.categoryEntity.categoryName ORDER BY COUNT(d) DESC")
    java.util.List<Object[]> countGroupByCategory();

    @org.springframework.data.jpa.repository.Query("SELECT YEAR(d.createdAt), MONTH(d.createdAt), COUNT(d) FROM DocumentEntity d WHERE d.isDeleted = false AND d.createdAt >= :from GROUP BY YEAR(d.createdAt), MONTH(d.createdAt) ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)")
    java.util.List<Object[]> countCreatedByMonth(@org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from);

    Optional<DocumentEntity> findFirstByFolderEntity_IdFolderAndTypeDocumentAndIsDeletedFalse(
            String idFolder, TypeDocument typeDocument);

    Optional<DocumentEntity> findFirstByCategoryEntity_IdCategoryAndTypeDocumentAndIsDeletedFalse(
            String idCategory, TypeDocument typeDocument);

    List<DocumentEntity> findByCategoryEntity_IdCategoryAndIsDeletedFalse(String idCategory);

}
