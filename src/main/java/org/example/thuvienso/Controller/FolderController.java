package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.FolderRequest;
import org.example.thuvienso.Dto.Response.Folder.ChildFolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponse;
import org.example.thuvienso.Dto.Response.Folder.FolderResponseNoList;
import org.example.thuvienso.Service.FolderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FolderController {
    FolderService folderService;
    @PostMapping
    public ApiResponse<FolderResponse> createFolder(@RequestBody @Valid FolderRequest request) {
        return ApiResponse.<FolderResponse>builder()
                .code(0)
                .message("Tạo thư mục thành công")
                .success(true)
                .Result(folderService.create(request))
                .build();
    }

    @GetMapping("/allTree/{id}")
    public ApiResponse<List<ChildFolderResponse>> getTreeFolderById(@PathVariable("id") String id) {
        return ApiResponse.<List<ChildFolderResponse>>builder()
                .code(0)
                .message("Lấy cây thư mục thành công")
                .success(true)
                .Result(folderService.getAllTree(id))
                .build();
    }
    @GetMapping("/getChildFolder/{id}")
    public ApiResponse<List<FolderResponseNoList>> getAllFolder(@PathVariable("id") String id){
        return ApiResponse.<List<FolderResponseNoList>>builder()
                .code(0)
                .message("Lấy danh sách thư mục con thành công")
                .success(true)
                .Result(folderService.getAllChildFolder(id))
                .build();
    }
}
