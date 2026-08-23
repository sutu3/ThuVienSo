package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepo extends JpaRepository<FolderEntity,String> {
    List<FolderEntity> findALlByIsDeleted(Boolean isDeleted);
    List<FolderEntity> findByParentFolderIsNull();
    Optional<FolderEntity> findByFolderName(String folderName);
}
