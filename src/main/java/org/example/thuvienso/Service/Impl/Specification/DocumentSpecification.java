package org.example.thuvienso.Service.Impl.Specification;

import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {
    public static Specification<DocumentEntity> hasTitle(String title) {
        return (root, query, criteriaBuilder) ->
                title == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }
}
