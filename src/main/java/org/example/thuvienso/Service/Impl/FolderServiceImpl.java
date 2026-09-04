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
import org.example.thuvienso.Helper.FolderServiceHelper;
import org.example.thuvienso.Mapper.FolderMapper;
import org.example.thuvienso.Module.FolderEntity;
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
    FolderRepo folderRepo;
    FolderMapper folderMapper;
    FolderServiceHelper folderServiceHelper;

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

        FolderEntity newRoot = folderServiceHelper.deepCopyFolder(source, targetParent,true);
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
            if (folderServiceHelper.isSameOrDescendant(source, targetParent)) {
                throw new AppException(ErrorCode.FOLDER_NOT_FOUND); // nên thêm mã riêng, vd INVALID_MOVE_TARGET
            }
        }

        source.setParentFolder(targetParent);   // chỉ cần đúng 1 dòng này
        source.setUpdatedAt(LocalDateTime.now());
        folderRepo.save(source);

        return folderMapper.toResponse(source);
    }



    @Override
    @Transactional
    public void hardDeleteFolder(String id) {
        FolderEntity folder = getById(id);

        // Chỉ cho xóa cứng folder ĐÃ xóa mềm
        if (folder.getIsDeleted() == null || !folder.getIsDeleted()) {
            throw new AppException(ErrorCode.FOLDER_NOT_DELETED);
        }
        folderServiceHelper.deleteFolderRecursively(folder);
    }



}
