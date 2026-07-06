package org.example.thuvienso.Repo;

import org.example.thuvienso.Enum.TypeCollection;
import org.example.thuvienso.Module.CollectionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepo extends JpaRepository<CollectionEntity,String>, JpaSpecificationExecutor<CollectionEntity> {
    Optional<CollectionEntity> findByTypeCollection(TypeCollection typeCollection);
}
