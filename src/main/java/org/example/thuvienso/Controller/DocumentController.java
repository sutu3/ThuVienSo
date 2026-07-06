package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.DocumentRequest;
import org.example.thuvienso.Dto.Response.Document.DocumentResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Form.DocumentForm;
import org.example.thuvienso.Service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentController {
    DocumentService documentService;

    @PostMapping
    public ApiResponse<DocumentResponse> createDocument(
            @RequestBody @Valid DocumentRequest request
    ) {

        return ApiResponse.<DocumentResponse>builder()
                .code(0)
                .message("Tạo tài liệu thành công")
                .success(true)
                .Result(documentService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponse> getDocumentById(
            @PathVariable("id") String id
    ) {

        return ApiResponse.<DocumentResponse>builder()
                .code(0)
                .message("Lấy thông tin tài liệu thành công")
                .success(true)
                .Result(documentService.getByIdResponse(id))
                .build();
    }

    @GetMapping("/folder/{idFolder}")
    public ApiResponse<List<DocumentResponseNoList>> getListByFolder(
            @PathVariable String idFolder
    ) {

        return ApiResponse.<List<DocumentResponseNoList>>builder()
                .code(0)
                .message("Lấy danh sách tài liệu theo thư mục thành công")
                .success(true)
                .Result(documentService.getListByIdFolder(idFolder))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponse> updateDocument(
            @PathVariable("id") String id,
            @RequestBody DocumentForm update
    ) {

        return ApiResponse.<DocumentResponse>builder()
                .code(0)
                .message("Cập nhật tài liệu thành công")
                .success(true)
                .Result(documentService.update(id, update))
                .build();
    }
}
