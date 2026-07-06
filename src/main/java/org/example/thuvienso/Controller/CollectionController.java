package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.CollectionRequest;
import org.example.thuvienso.Dto.Response.Collection.CollectionResponse;
import org.example.thuvienso.Dto.Response.Document.DocumentResponseNoList;
import org.example.thuvienso.Service.CollectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollectionController {

    CollectionService collectionService;

    @PostMapping
    public ApiResponse<CollectionResponse> createCollection(
            @RequestBody @Valid CollectionRequest request
    ) {

        return ApiResponse.<CollectionResponse>builder()
                .code(0)
                .message("Tạo bộ sưu tập thành công")
                .success(true)
                .Result(collectionService.create(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CollectionResponse> getCollectionById(
            @PathVariable("id") String id
    ) {

        return ApiResponse.<CollectionResponse>builder()
                .code(0)
                .message("Lấy thông tin bộ sưu tập thành công")
                .success(true)
                .Result(collectionService.getByIdResponse(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CollectionResponse>> getAllCollection() {

        return ApiResponse.<List<CollectionResponse>>builder()
                .code(0)
                .message("Lấy danh sách bộ sưu tập thành công")
                .success(true)
                .Result(collectionService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CollectionResponse> updateCollection(
            @PathVariable("id") String id,
            @RequestBody CollectionRequest request
    ) {

        return ApiResponse.<CollectionResponse>builder()
                .code(0)
                .message("Cập nhật bộ sưu tập thành công")
                .success(true)
                .Result(collectionService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCollection(
            @PathVariable("id") String id
    ) {

        collectionService.delete(id);

        return ApiResponse.<String>builder()
                .code(0)
                .message("Xóa bộ sưu tập thành công")
                .success(true)
                .Result("OK")
                .build();
    }

    @PostMapping("/{idCollection}/documents/{idDocument}")
    public ApiResponse<CollectionResponse> addDocumentToCollection(
            @PathVariable String idCollection,
            @PathVariable String idDocument
    ) {

        return ApiResponse.<CollectionResponse>builder()
                .code(0)
                .message("Thêm tài liệu vào bộ sưu tập thành công")
                .success(true)
                .Result(
                        collectionService.addDocument(
                                idCollection,
                                idDocument
                        )
                )
                .build();
    }

    @DeleteMapping("/{idCollection}/documents/{idDocument}")
    public ApiResponse<CollectionResponse> removeDocumentFromCollection(
            @PathVariable String idCollection,
            @PathVariable String idDocument
    ) {

        return ApiResponse.<CollectionResponse>builder()
                .code(0)
                .message("Xóa tài liệu khỏi bộ sưu tập thành công")
                .success(true)
                .Result(
                        collectionService.removeDocument(
                                idCollection,
                                idDocument
                        )
                )
                .build();
    }

    @GetMapping("/{id}/documents")
    public ApiResponse<List<DocumentResponseNoList>> getDocumentsByCollection(
            @PathVariable String id
    ) {

        return ApiResponse.<List<DocumentResponseNoList>>builder()
                .code(0)
                .message("Lấy danh sách tài liệu thành công")
                .success(true)
                .Result(collectionService.getDocuments(id))
                .build();
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<CollectionResponse>> getCollectionByType(
            @PathVariable String type
    ) {

        return ApiResponse.<List<CollectionResponse>>builder()
                .code(0)
                .message("Lấy bộ sưu tập theo loại thành công")
                .success(true)
                .Result(collectionService.getByType(type))
                .build();
    }

}
