package org.example.thuvienso.Helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Module.FolderEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Repo.FolderRepo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FolderServiceHelper {
    LocalStorage localStorage;
    DocumentRepo documentRepo;
    FileRepo fileRepo;
    FolderRepo folderRepo;

    // target có trùng source hoặc nằm trong cây con của source không?
    public boolean isSameOrDescendant(FolderEntity source, FolderEntity target) {
        FolderEntity cur = target;
        while (cur != null) {
            if (cur.getIdFolder().equals(source.getIdFolder())) return true;
            cur = cur.getParentFolder();   // parentFolder EAGER nên đi ngược lên được
        }
        return false;
    }

    // Nhân bản cả cây con: folder + document + file
    public FolderEntity deepCopyFolder(FolderEntity source, FolderEntity parent,boolean reName) {
        FolderEntity copy = FolderEntity.builder()
                .folderName(reName?source.getFolderName() + " - Copy":source.getFolderName())
                .description(source.getDescription())
                .parentFolder(parent)
                .build();
        copy.setCreatedAt(LocalDateTime.now());
        copy.setIsDeleted(false);
        folderRepo.save(copy);

        // 1) copy document trong folder này
        documentRepo.findByFolderEntity_IdFolder(source.getIdFolder()).stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .forEach(d -> copyDocumentEntity(d, copy));

        // 2) đệ quy sang folder con
        if (source.getChildFolder() != null) {
            source.getChildFolder().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                    .forEach(child -> deepCopyFolder(child, copy,false));
        }
        return copy;
    }

    private void deletePhysicalFile(String objectName) {
        if (objectName == null || objectName.isBlank()) return;
        // icon dùng chung (icons/...) không được xóa
        if (objectName.startsWith("icons/")) return;
        try {
            localStorage.delete(objectName);
        } catch (Exception e) {
            log.error("Không thể xóa file vật lý {}: {}", objectName, e.getMessage());
        }
    }
    public void deleteFolderRecursively(FolderEntity folder) {
        // 1. Đệ quy xóa folder con trước
        List<FolderEntity> children = folder.getChildFolder();
        if (children != null) {
            // copy ra list mới để tránh ConcurrentModification khi xóa
            for (FolderEntity child : List.copyOf(children)) {
                deleteFolderRecursively(child);
            }
        }

        // 2. Xóa toàn bộ document + file trong folder này
        List<DocumentEntity> documents =
                documentRepo.findByFolderEntity_IdFolder(folder.getIdFolder());
        for (DocumentEntity doc : documents) {
            List<FileEntity> files =
                    fileRepo.findByDocumentEntity_IdDocument(doc.getIdDocument());
            for (FileEntity file : files) {
                deletePhysicalFile(file.getPartFile());
                deletePhysicalFile(file.getThumbnail()); // xóa cả thumbnail (bỏ qua icon dùng chung)
                fileRepo.delete(file);
            }
            documentRepo.delete(doc);
        }

        // 3. Xóa chính folder
        folderRepo.delete(folder);
    }

    // Dùng chung cho copyFolder và copyDocument
    public DocumentEntity copyDocumentEntity(DocumentEntity source, FolderEntity targetFolder) {
        DocumentEntity newDoc = DocumentEntity.builder()
                .content(source.getContent())
                .title(source.getTitle())
                .status(source.getStatus())
                .typeDocument(source.getTypeDocument())
                .thumbnail(source.getThumbnail())
                .viewCount(0L)
                .downloadCount(0L)
                .categoryEntity(source.getCategoryEntity())
                .folderEntity(targetFolder)
                .build();
        newDoc.setCreatedAt(LocalDateTime.now());
        newDoc.setIsDeleted(false);
        documentRepo.save(newDoc);

        // copy file (trỏ lại cùng partFile trên storage - không upload lại)
        fileRepo.findAllByDocumentEntity_IdDocument(source.getIdDocument()).stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .forEach(f -> {
                    FileEntity newFile = FileEntity.builder()
                            .fileName(f.getFileName())
                            .partFile(f.getPartFile())
                            .typeFile(f.getTypeFile())
                            .thumbnail(f.getThumbnail())
                            .documentEntity(newDoc)
                            .build();
                    newFile.setCreatedAt(LocalDateTime.now());
                    newFile.setIsDeleted(false);
                    fileRepo.save(newFile);
                });
        return newDoc;
    }


}
