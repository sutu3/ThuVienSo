package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.DocumentEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentRepo extends JpaRepository<DocumentEntity,String>, JpaSpecificationExecutor<DocumentEntity> {
    List<DocumentEntity> findByFolderEntity_IdFolder(String idFolder);
}
