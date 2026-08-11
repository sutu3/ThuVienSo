package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.BookRequest;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Form.BookForm;
import org.example.thuvienso.Service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {
    BookService bookService;

    @PostMapping
    public ApiResponse<BookResponse> createBook(@RequestBody @Valid BookRequest request) {
        return ApiResponse.<BookResponse>builder()
                .code(0).success(true).message("Tạo sách thành công")
                .Result(bookService.createBook(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable("id") String id) {
        return ApiResponse.<BookResponse>builder()
                .code(0).success(true).message("Lấy thông tin sách thành công")
                .Result(bookService.getByIdResponse(id))
                .build();
    }

    @GetMapping("/code/{bookCode}")
    public ApiResponse<BookResponse> getByCode(@PathVariable("bookCode") String bookCode) {
        return ApiResponse.<BookResponse>builder()
                .code(0).success(true).message("Tra cứu sách theo mã thành công")
                .Result(bookService.getByBookCode(bookCode))
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<List<BookResponse>> getAllBook() {
        return ApiResponse.<List<BookResponse>>builder()
                .code(0).success(true).message("Lấy danh sách sách thành công")
                .Result(bookService.getAll())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBook(@PathVariable("id") String id) {
        bookService.deletedById(id);
        return ApiResponse.<String>builder()
                .code(0).success(true).message("Xóa sách thành công")
                .Result("Deleted book with id: " + id)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(
            @PathVariable("id") String id,
            @RequestBody @Valid BookForm form
    ) {
        return ApiResponse.<BookResponse>builder()
                .code(0).success(true).message("Cập nhật sách thành công")
                .Result(bookService.updateBook(form, id))
                .build();
    }
}