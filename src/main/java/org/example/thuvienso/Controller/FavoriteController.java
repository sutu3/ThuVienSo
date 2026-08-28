package org.example.thuvienso.Controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteController {

    FavoriteService favoriteService;

    @PostMapping("/{idBook}")
    public ApiResponse<Void> add(@PathVariable("idBook") String idBook) {
        favoriteService.addFavorite(idBook);
        return ApiResponse.<Void>builder()
                .code(0).success(true).message("Đã thêm vào yêu thích")
                .build();
    }

    @DeleteMapping("/{idBook}")
    public ApiResponse<Void> remove(@PathVariable("idBook") String idBook) {
        favoriteService.removeFavorite(idBook);
        return ApiResponse.<Void>builder()
                .code(0).success(true).message("Đã xóa khỏi yêu thích")
                .build();
    }

    @GetMapping("/my")
    public ApiResponse<List<BookResponse>> myFavorites() {
        return ApiResponse.<List<BookResponse>>builder()
                .code(0).success(true).message("Lấy danh sách yêu thích thành công")
                .Result(favoriteService.getMyFavorites())
                .build();
    }
}