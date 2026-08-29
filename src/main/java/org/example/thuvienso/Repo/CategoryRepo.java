package org.example.thuvienso.Repo;

import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryEntity,String>, JpaSpecificationExecutor<CategoryEntity> {
    Optional<CategoryEntity> findByCategoryName(String categoryName);
    @Query("""  
        SELECT DISTINCT d.categoryEntity FROM DocumentEntity d  
        WHERE d.typeDocument = :type  
          AND d.isDeleted = false  
          AND d.categoryEntity.isDeleted = false  
        """)
    List<CategoryEntity> findCategoriesByDocumentType(@Param("type") TypeDocument type);
    List<CategoryEntity> findByParentCategoryIsNull();
    List<CategoryEntity> findByParentCategory_IdCategory(String parentId);
    boolean existsByParentCategory_IdCategoryAndCategoryName(String parentId, String categoryName);

}
