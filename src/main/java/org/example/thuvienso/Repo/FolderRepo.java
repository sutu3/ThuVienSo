package org.example.thuvienso.Repo;

import org.example.thuvienso.Module.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepo extends JpaRepository<FolderEntity,String> {
    List<FolderEntity> findALlByIsDeleted(Boolean isDeleted);
}
