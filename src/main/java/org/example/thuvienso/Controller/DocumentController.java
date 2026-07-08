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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletedById(
            @PathVariable("id") String id
    ) {

        documentService.deletedById(id);

        return ApiResponse.<Void>builder()
                .code(0)
                .message("Xóa tài liệu thành công")
                .success(true)
                .build();
    }

    @GetMapping("/deleted")
    public ApiResponse<List<DocumentResponse>> getAllDocumentDeleted() {

        return ApiResponse.<List<DocumentResponse>>builder()
                .code(0)
                .message("Lấy danh sách tài liệu đã xóa thành công")
                .success(true)
                .Result(documentService.getAllDocumentDeleted())
                .build();
    }

    @PutMapping("/restore/{id}")
    public ApiResponse<DocumentResponse> restoreDocument(
            @PathVariable("id") String id
    ) {

        return ApiResponse.<DocumentResponse>builder()
                .code(0)
                .message("Khôi phục tài liệu thành công")
                .success(true)
                .Result(documentService.restoreDocument(id))
                .build();
    }

    @GetMapping("/page")
    public ApiResponse<Page<DocumentResponse>> getAllByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<DocumentResponse>>builder()
                .code(0)
                .message("Lấy danh sách tài liệu phân trang thành công")
                .success(true)
                .Result(documentService.getAllByPage(pageable))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<DocumentResponse>> searchByTitle(
            @RequestParam("keyword") String keyword
    ) {

        return ApiResponse.<List<DocumentResponse>>builder()
                .code(0)
                .message("Tìm kiếm tài liệu thành công")
                .success(true)
                .Result(
                        documentService
                                .searchByTitleContainingIgnoreCase(keyword)
                )
                .build();
    }
}
