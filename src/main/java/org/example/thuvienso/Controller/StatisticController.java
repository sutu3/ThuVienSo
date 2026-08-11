package org.example.thuvienso.Controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.Statistic.CountByKeyResponse;
import org.example.thuvienso.Dto.Response.Statistic.StatisticResponse;
import org.example.thuvienso.Service.StatisticService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistic")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticController {

    StatisticService statisticService;

    @GetMapping("/overview")
    public ApiResponse<StatisticResponse> overview() {
        return ApiResponse.<StatisticResponse>builder()
                .code(0).success(true)
                .message("Thống kê tổng quan thành công")
                .Result(statisticService.getOverview())
                .build();
    }

    @GetMapping("/documentByType")
    public ApiResponse<List<CountByKeyResponse>> documentByType() {
        return ApiResponse.<List<CountByKeyResponse>>builder()
                .code(0).success(true)
                .message("Thống kê tài liệu theo loại thành công")
                .Result(statisticService.countDocumentByType())
                .build();
    }

    @GetMapping("/topViewed")
    public ApiResponse<List<CountByKeyResponse>> topViewed(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<CountByKeyResponse>>builder()
                .code(0).success(true)
                .message("Thống kê tài liệu xem nhiều nhất thành công")
                .Result(statisticService.topViewedDocuments(limit))
                .build();
    }
}