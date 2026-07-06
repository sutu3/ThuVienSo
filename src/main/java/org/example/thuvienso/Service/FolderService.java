package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.Folder.ChildFolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Module.FolderEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FolderService {
    FolderResponse create(FolderRequest request);
    List<ChildFolderResponse> getAllTree(String idFolder);
    List<FolderResponseNoList> getAllChildFolder(String idFolder);
    FolderEntity getById(String id);

}
