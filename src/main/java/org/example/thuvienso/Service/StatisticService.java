package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Response.Statistic.CountByKeyResponse;
import org.example.thuvienso.Dto.Response.Statistic.StatisticResponse;

import java.util.List;

public interface StatisticService {
    StatisticResponse getOverview();                 // tổng quan tất cả

    List<CountByKeyResponse> countDocumentByType();  // biểu đồ theo loại tài liệu

    List<CountByKeyResponse> topViewedDocuments(int limit); // top xem nhiều

    List<CountByKeyResponse> countDocumentByStatus();

    List<CountByKeyResponse> topCategories(int limit);

    List<CountByKeyResponse> countUsersByRole();

    List<CountByKeyResponse> monthlyTrend(int months);

    List<CountByKeyResponse> weeklyActivity();
}
