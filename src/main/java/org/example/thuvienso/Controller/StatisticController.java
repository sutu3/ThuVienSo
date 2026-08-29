package org.example.thuvienso.Controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.example.thuvienso.Dto.Response.Statistic.CountByKeyResponse;
import org.example.thuvienso.Dto.Response.Statistic.StatisticResponse;
import org.example.thuvienso.Service.StatisticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/documentByStatus")
    public ApiResponse<List<CountByKeyResponse>> documentByStatus() {
        return response("Thống kê tài liệu theo trạng thái thành công", statisticService.countDocumentByStatus());
    }

    @GetMapping("/topCategories")
    public ApiResponse<List<CountByKeyResponse>> topCategories(@RequestParam(defaultValue = "10") int limit) {
        return response("Thống kê danh mục nhiều tài liệu thành công", statisticService.topCategories(limit));
    }

    @GetMapping("/usersByRole")
    public ApiResponse<List<CountByKeyResponse>> usersByRole() {
        return response("Thống kê người dùng theo vai trò thành công", statisticService.countUsersByRole());
    }

    @GetMapping("/monthlyTrend")
    public ApiResponse<List<CountByKeyResponse>> monthlyTrend(@RequestParam(defaultValue = "12") int months) {
        return response("Thống kê xu hướng tài liệu theo tháng thành công", statisticService.monthlyTrend(months));
    }

    @GetMapping("/weeklyActivity")
    public ApiResponse<List<CountByKeyResponse>> weeklyActivity() {
        return response("Thống kê hoạt động 7 ngày gần nhất thành công", statisticService.weeklyActivity());
    }

    private ApiResponse<List<CountByKeyResponse>> response(String message, List<CountByKeyResponse> result) {
        return ApiResponse.<List<CountByKeyResponse>>builder().code(0).success(true).message(message).Result(result).build();
    }
}
