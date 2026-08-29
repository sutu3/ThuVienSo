package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.thuvienso.Dto.Response.Statistic.CountByKeyResponse;
import org.example.thuvienso.Dto.Response.Statistic.StatisticResponse;
import org.example.thuvienso.Enum.BorrowStatus;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;
import org.example.thuvienso.Module.DocumentEntity;
import org.example.thuvienso.Repo.BookRepo;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.AuditLogRepo;
import org.example.thuvienso.Repo.BorrowRecordRepo;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.StatisticService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticServiceImpl implements StatisticService {

    DocumentRepo documentRepo;
    FileRepo fileRepo;
    BookRepo bookRepo;
    BorrowRecordRepo borrowRepo;
    AccountRepo accountRepo;
    AuditLogRepo auditLogRepo;

    @Override
    public StatisticResponse getOverview() {
        StatisticResponse.StatisticResponseBuilder b = StatisticResponse.builder()
                // ==== Tài liệu số ====
                .totalDocuments(documentRepo.countByIsDeletedFalse())
                .pendingDocuments(documentRepo.countByIsDeletedFalseAndStatus(StatusDocument.Pending))
                .approvedDocuments(documentRepo.countByIsDeletedFalseAndStatus(StatusDocument.Approve))
                .totalViews(documentRepo.sumViewCount())
                .totalDownloads(documentRepo.sumDownloadCount())
                .totalFiles(fileRepo.countByIsDeletedFalse());

        b.totalBooks(bookRepo.countByIsDeletedFalse())
                .totalBookCopies(bookRepo.sumTotalCopies())
                .availableBookCopies(bookRepo.sumAvailableCopies());

        b.totalBorrows(borrowRepo.count())
                .borrowingCount(borrowRepo.countByStatus(BorrowStatus.BORROWED))
                .overdueCount(borrowRepo.countByStatus(BorrowStatus.OVERDUE))
                .returnedCount(borrowRepo.countByStatus(BorrowStatus.RETURNED));

        return b.build();
    }

    @Override
    public List<CountByKeyResponse> countDocumentByType() {
        return documentRepo.countGroupByType().stream()
                .map(row -> CountByKeyResponse.builder()
                        .key(((TypeDocument) row[0]).name())
                        .value(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountByKeyResponse> topViewedDocuments(int limit) {
        return documentRepo
                .findByIsDeletedFalseOrderByViewCountDesc(PageRequest.of(0, boundedLimit(limit)))
                .stream()
                .map((DocumentEntity d) -> CountByKeyResponse.builder()
                        .key(d.getTitle())
                        .value(d.getViewCount() == null ? 0L : d.getViewCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountByKeyResponse> countDocumentByStatus() {
        Map<StatusDocument, Long> counts = new EnumMap<>(StatusDocument.class);
        documentRepo.countGroupByStatus().forEach(row ->
                counts.put((StatusDocument) row[0], ((Number) row[1]).longValue())
        );
        return Arrays.stream(StatusDocument.values())
                .map(status -> CountByKeyResponse.builder()
                        .key(status.name())
                        .value(counts.getOrDefault(status, 0L))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountByKeyResponse> topCategories(int limit) {
        return documentRepo.countGroupByCategory().stream()
                .limit(boundedLimit(limit))
                .map(row -> CountByKeyResponse.builder()
                        .key((String) row[0])
                        .value(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountByKeyResponse> countUsersByRole() {
        return accountRepo.countGroupByRole().stream()
                .map(row -> CountByKeyResponse.builder()
                        .key((String) row[0])
                        .value(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountByKeyResponse> monthlyTrend(int months) {
        int safeMonths = Math.min(Math.max(months, 1), 36);
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(safeMonths - 1L);
        Map<String, Long> counts = new HashMap<>();
        documentRepo.countCreatedByMonth(firstMonth.atDay(1).atStartOfDay()).forEach(row -> {
            String key = String.format("%04d-%02d", ((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            counts.put(key, ((Number) row[2]).longValue());
        });

        List<CountByKeyResponse> result = new ArrayList<>();
        for (int i = 0; i < safeMonths; i++) {
            YearMonth month = firstMonth.plusMonths(i);
            String key = month.toString();
            result.add(CountByKeyResponse.builder().key(key).value(counts.getOrDefault(key, 0L)).build());
        }
        return result;
    }

    @Override
    public List<CountByKeyResponse> weeklyActivity() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.minusDays(6);
        Map<String, Long> counts = new HashMap<>();
        auditLogRepo.countActivityByDay(firstDay.atStartOfDay()).forEach(row ->
                counts.put(row[0].toString(), ((Number) row[1]).longValue())
        );

        List<CountByKeyResponse> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 0; i < 7; i++) {
            LocalDate day = firstDay.plusDays(i);
            result.add(CountByKeyResponse.builder()
                    .key(day.format(formatter))
                    .value(counts.getOrDefault(day.toString(), 0L))
                    .build());
        }
        return result;
    }

    private int boundedLimit(int limit) {
        return Math.min(Math.max(limit, 1), 100);
    }
}
