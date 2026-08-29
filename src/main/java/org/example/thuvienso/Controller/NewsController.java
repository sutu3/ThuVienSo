package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.NewsRequest;
import org.example.thuvienso.Dto.Response.News.NewsResponse;
import org.example.thuvienso.Service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public ApiResponse<Page<NewsResponse>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String categoryId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ok("Lấy danh sách tin tức thành công", newsService.getPublished(keyword, categoryId, null, page, size));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<NewsResponse> detail(@PathVariable String slug) {
        return ok("Lấy bài viết thành công", newsService.getPublishedBySlug(slug));
    }

    @PostMapping
    public ApiResponse<NewsResponse> create(@RequestBody @Valid NewsRequest request) {
        return ok("Tạo bài viết thành công", newsService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<NewsResponse> update(@PathVariable String id, @RequestBody @Valid NewsRequest request) {
        return ok("Cập nhật bài viết thành công", newsService.update(id, request));
    }
    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> getByIdNew(@PathVariable String id) {
        return ok("Lấy bài viết thành công", newsService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        newsService.delete(id);
        return ok("Xóa bài viết thành công", null);
    }

    @GetMapping("/admin/list")
    public ApiResponse<Page<NewsResponse>> adminList(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ok("Lấy danh sách quản trị thành công", newsService.getForAdmin(keyword, status, page, size));
    }

    private <T> ApiResponse<T> ok(String message, T result) {
        return ApiResponse.<T>builder().success(true).code(0).message(message).Result(result).build();
    }
    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return ok("Upload ảnh thành công", Map.of("url", newsService.uploadImage(file)));
    }
}
