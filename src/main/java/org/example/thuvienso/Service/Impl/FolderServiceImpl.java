package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.Folder.ChildFolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
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
    private final FolderRepo folderRepo;
    FolderMapper folderMapper;
    @Override
    public FolderResponse create(FolderRequest request) {
        FolderEntity folder=folderMapper.toEntity(request);
        if(request.getParentFolder()!=null){
            FolderEntity parent=getById(request.getParentFolder());
            folder.setParentFolder(parent);

        }
        folder.setCreatedAt(LocalDateTime.now());
        folder.setIsDeleted(false);

        return folderMapper.toResponse(folderRepo.save(folder));
    }

    @Override
    public List<ChildFolderResponse> getAllTree(String idFolder) {
        FolderEntity folder=getById(idFolder);

        return folder.getChildFolder().stream()

                .map(folderMapper::toChildResponse).collect(Collectors.toList());
    }

    @Override
    public List<FolderResponseNoList> getAllChildFolder(String idFolder) {
        FolderEntity folder=getById(idFolder);
        return folder.getChildFolder().stream()
                .map(folderMapper::toResponseNoList)
                .collect(Collectors.toList());
    }

    @Override
    public FolderEntity getById(String id) {
        return folderRepo.findById(id).orElseThrow(()->
                new AppException(ErrorCode.FOLDER_NOT_FOUND));
    }
}
