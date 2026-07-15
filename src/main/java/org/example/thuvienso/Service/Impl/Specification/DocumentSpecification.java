package org.example.thuvienso.Service.Impl.Specification;

import jakarta.persistence.criteria.Predicate;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DocumentSpecification {
    public static Specification<DocumentEntity> hasTitle(String title) {
        return (root, query, cb) ->
                title == null ? null
                        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<DocumentEntity> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String like = "%" + keyword.toLowerCase() + "%";
            Predicate onTitle = cb.like(cb.lower(root.get("title")), like);
            Predicate onContent = cb.like(cb.lower(root.get("content")), like);
            return cb.or(onTitle, onContent);
        };
    }

    public static Specification<DocumentEntity> hasType(TypeDocument type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("typeDocument"), type);
    }

    public static Specification<DocumentEntity> hasStatus(StatusDocument status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<DocumentEntity> hasCategory(String idCategory) {
        return (root, query, cb) ->
                idCategory == null ? null
                        : cb.equal(root.get("categoryEntity").get("idCategory"), idCategory);
    }

    public static Specification<DocumentEntity> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<DocumentEntity> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    // luôn loại bản ghi đã xóa mềm (thay cho .filter(!isDeleted) ở service)
    public static Specification<DocumentEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }
}
