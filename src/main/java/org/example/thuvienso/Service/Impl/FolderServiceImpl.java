package org.example.thuvienso.Service.Impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.CopyFolderRequest;
import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.Folder.ChildFolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Form.FolderForm;
import org.example.thuvienso.Helper.LocalStorage;
import org.example.thuvienso.Mapper.FolderMapper;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Module.FileEntity;
import org.example.thuvienso.Module.FolderEntity;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Repo.FolderRepo;
import org.example.thuvienso.Service.FolderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FolderServiceImpl implements FolderService {
    private final FolderRepo folderRepo;
    FolderMapper folderMapper;
    DocumentRepo documentRepo;
    FileRepo fileRepo;
    LocalStorage localStorage;

    @Override
    public FolderResponse create(FolderRequest request) {
        FolderEntity folder = folderMapper.toEntity(request);
        if (request.getParentFolder() != null) {
            FolderEntity parent = getById(request.getParentFolder());
            folder.setParentFolder(parent);

        }
        folder.setCreatedAt(LocalDateTime.now());
        folder.setIsDeleted(false);

        return folderMapper.toResponse(folderRepo.save(folder));
    }

    @Override
    public List<ChildFolderResponse> getAllTree(String idFolder) {
        FolderEntity folder = getById(idFolder);

        return folder.getChildFolder().stream()
                .filter(child -> !child.getIsDeleted())
                .map(folderMapper::toChildResponse).collect(Collectors.toList());
    }

    @Override
    public List<FolderResponseNoList> getAllChildFolder(String idFolder) {
        FolderEntity folder = getById(idFolder);
        return folder.getChildFolder().stream()
                .filter(child -> !child.getIsDeleted())
                .map(folderMapper::toResponseNoList)
                .collect(Collectors.toList());
    }

    @Override
    public FolderEntity getById(String id) {
        return folderRepo.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.FOLDER_NOT_FOUND));
    }

    @Override
    public void deletedById(String id) {
        FolderEntity folder = getById(id);
        folder.setIsDeleted(true);
        folder.setDeletedAt(LocalDateTime.now());
        folderRepo.save(folder);
    }

    @Override
    public List<FolderResponseNoList> getAllFolderDeleted() {
        return folderRepo.findALlByIsDeleted(true)
                .stream().map(folderMapper::toResponseNoList)
                .collect(Collectors.toList());
    }

    @Override
    public FolderResponse restoreFolder(String id) {
        FolderEntity folder = getById(id);
        folder.setIsDeleted(false);
        folderRepo.save(folder);
        return folderMapper.toResponse(folder);
    }

    @Override
    public FolderResponse updateFolder(FolderForm update, String idFolder) {
        FolderEntity folder = getById(idFolder);
        if(folderRepo.existsByParentFolder_IdFolderAndFolderName(folder.getParentFolder().getIdFolder(),update.getFolderName())) throw new AppException(ErrorCode.FOLDER_IS_EXIST);
        folderMapper.update(folder, update);
        folder.setUpdatedAt(LocalDateTime.now());
        return folderMapper.toResponse(folderRepo.save(folder));
    }

    @Override
    public FolderResponseNoList getAllFolderLevel1() {
        return folderMapper.toResponseNoList(folderRepo.findByFolderName("Sư đoàn 5")
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND)));
    }

    @Override
    @Transactional
    public FolderResponse copyFolder(String idFolder, CopyFolderRequest copy) {
        FolderEntity source = getById(idFolder);

        FolderEntity targetParent = null;
        if (copy.getParentFolder() != null && !copy.getParentFolder().isBlank()) {
            targetParent = getById(copy.getParentFolder());
        }

        FolderEntity newRoot = deepCopyFolder(source, targetParent,true);
        return folderMapper.toResponse(newRoot);
    }

    @Override
    @Transactional
    public FolderResponse cutFolder(String idFolder, CopyFolderRequest cut) {
        FolderEntity source = getById(idFolder);
        if(folderRepo.existsByParentFolder_IdFolderAndFolderName(cut.getParentFolder(),source.getFolderName())) throw new AppException(ErrorCode.FOLDER_IS_EXIST);

        FolderEntity targetParent = null;
        if (cut.getParentFolder() != null && !cut.getParentFolder().isBlank()) {
            targetParent = getById(cut.getParentFolder());

            // chặn move vào chính nó hoặc vào hậu duệ của nó
            if (isSameOrDescendant(source, targetParent)) {
                throw new AppException(ErrorCode.FOLDER_NOT_FOUND); // nên thêm mã riêng, vd INVALID_MOVE_TARGET
            }
        }

        source.setParentFolder(targetParent);   // chỉ cần đúng 1 dòng này
        source.setUpdatedAt(LocalDateTime.now());
        folderRepo.save(source);

        return folderMapper.toResponse(source);
    }

    // target có trùng source hoặc nằm trong cây con của source không?
    private boolean isSameOrDescendant(FolderEntity source, FolderEntity target) {
        FolderEntity cur = target;
        while (cur != null) {
            if (cur.getIdFolder().equals(source.getIdFolder())) return true;
            cur = cur.getParentFolder();   // parentFolder EAGER nên đi ngược lên được
        }
        return false;
    }

    // Nhân bản cả cây con: folder + document + file
    private FolderEntity deepCopyFolder(FolderEntity source, FolderEntity parent,boolean reName) {
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

    // Dùng chung cho copyFolder và copyDocument
    private DocumentEntity copyDocumentEntity(DocumentEntity source, FolderEntity targetFolder) {
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
    @Override
    @Transactional
    public void hardDeleteFolder(String id) {
        FolderEntity folder = getById(id);

        // Chỉ cho xóa cứng folder ĐÃ xóa mềm
        if (folder.getIsDeleted() == null || !folder.getIsDeleted()) {
            throw new AppException(ErrorCode.FOLDER_NOT_DELETED);
        }
        deleteFolderRecursively(folder);
    }

    private void deleteFolderRecursively(FolderEntity folder) {
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

}
