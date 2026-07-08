package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.CategoryRequest;
import org.example.thuvienso.Dto.Response.Account.AccountResponse;
import org.example.thuvienso.Dto.Response.Category.CategoryResponse;
import org.example.thuvienso.Form.CategoryForm;
import org.example.thuvienso.Service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .code(0)
                .message("Tạo thể loại thành công")
                .success(true)
                .Result(categoryService.createCategory(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getAccountById(@PathVariable("id") String id) {
        return ApiResponse.<CategoryResponse>builder()
                .code(0)
                .message("Lấy thông tin thể loại thành công")
                .success(true)
                .Result(categoryService.getByIdResponse(id))
                .build();
    }
    @GetMapping("/getAll")
    public ApiResponse<List<CategoryResponse>> getAllCategory(){
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(0)
                .message("Lấy danh sách thể loại thành công")
                .success(true)
                .Result(categoryService.getAll())
                .build();
    }
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable("id") String id) {
        categoryService.DeletedById(id);

        return ApiResponse.<String>builder()
                .code(0)
                .message("Xóa thể loại thành công")
                .success(true)
                .Result("Deleted category with id: " + id)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable("id") String id,
            @RequestBody @Valid CategoryForm categoryForm
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(0)
                .message("Cập nhật thể loại thành công")
                .success(true)
                .Result(categoryService.updateCategory(categoryForm, id))
                .build();
    }
}
