package org.example.thuvienso.Service.Impl.Specification;

import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class NewSpecification {
    public static Specification<DocumentEntity> baseSpec() {
        return (root, query, cb) -> cb.and(cb.isFalse(root.get("isDeleted")), cb.equal(root.get("typeDocument"), TypeDocument.ARTICLE));
    }

    public static Specification<DocumentEntity> keywordSpec(String keyword) {
        String value = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(cb.like(cb.lower(root.get("title")), value), cb.like(cb.lower(root.get("content")), value));
    }

    public static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("createdAt")));
    }
}
