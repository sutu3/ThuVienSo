package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<CategoryEntity,String>, JpaSpecificationExecutor<CategoryEntity> {
    Optional<CategoryEntity> findByCategoryName(String categoryName);

}
