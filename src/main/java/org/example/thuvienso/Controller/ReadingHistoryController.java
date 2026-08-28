package org.example.thuvienso.Controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.Book.BookResponse;
import org.example.thuvienso.Service.ReadingHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/readingHistory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadingHistoryController {

    ReadingHistoryService readingHistoryService;

    @PostMapping("/{idBook}")
    public ApiResponse<Void> markRead(@PathVariable("idBook") String idBook) {
        readingHistoryService.markRead(idBook);
        return ApiResponse.<Void>builder()
                .code(0).success(true).message("Đã ghi nhận lịch sử đọc")
                .build();
    }

    @GetMapping("/my")
    public ApiResponse<List<BookResponse>> myHistory() {
        return ApiResponse.<List<BookResponse>>builder()
                .code(0).success(true).message("Lấy lịch sử đọc thành công")
                .Result(readingHistoryService.getMyHistory())
                .build();
    }
}