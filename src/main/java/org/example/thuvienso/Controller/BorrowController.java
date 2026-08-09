package org.example.thuvienso.Controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Request.BorrowRequest;
import org.example.thuvienso.Dto.Response.Borrow.BorrowResponse;
import org.example.thuvienso.Service.BorrowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowController {

    BorrowService borrowService;

    @PostMapping
    public ApiResponse<BorrowResponse> register(@RequestBody @Valid BorrowRequest request) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Đăng ký mượn thành công")
                .Result(borrowService.register(request)).build();
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<BorrowResponse> approve(@PathVariable("id") String id) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Duyệt phiếu mượn thành công")
                .Result(borrowService.approve(id)).build();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<BorrowResponse> reject(@PathVariable("id") String id) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Từ chối phiếu mượn")
                .Result(borrowService.reject(id)).build();
    }

    @PutMapping("/{id}/borrowed")
    public ApiResponse<BorrowResponse> markBorrowed(@PathVariable("id") String id) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Xác nhận đã giao sách")
                .Result(borrowService.markBorrowed(id)).build();
    }

    @PutMapping("/{id}/return")
    public ApiResponse<BorrowResponse> returnBook(@PathVariable("id") String id) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Trả sách thành công")
                .Result(borrowService.returnBook(id)).build();
    }

    @GetMapping("/getAll")
    public ApiResponse<List<BorrowResponse>> getAll() {
        return ApiResponse.<List<BorrowResponse>>builder()
                .code(0).success(true).message("Lấy danh sách phiếu mượn")
                .Result(borrowService.getAll()).build();
    }

    @GetMapping("/my/{idAccount}")
    public ApiResponse<List<BorrowResponse>> myBorrows(@PathVariable("idAccount") String idAccount) {
        return ApiResponse.<List<BorrowResponse>>builder()
                .code(0).success(true).message("Lấy phiếu mượn của tôi")
                .Result(borrowService.getMyBorrows(idAccount)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BorrowResponse> getById(@PathVariable("id") String id) {
        return ApiResponse.<BorrowResponse>builder()
                .code(0).success(true).message("Lấy chi tiết phiếu mượn")
                .Result(borrowService.getById(id)).build();
    }
}