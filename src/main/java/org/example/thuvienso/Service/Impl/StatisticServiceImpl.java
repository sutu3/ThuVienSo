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
import org.example.thuvienso.Repo.BorrowRecordRepo;
import org.example.thuvienso.Repo.DocumentRepo;
import org.example.thuvienso.Repo.FileRepo;
import org.example.thuvienso.Service.StatisticService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticServiceImpl implements StatisticService {

    DocumentRepo documentRepo;
    FileRepo fileRepo;
    BookRepo bookRepo;
    BorrowRecordRepo borrowRepo;

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
                .findByIsDeletedFalseOrderByViewCountDesc(PageRequest.of(0, limit))
                .stream()
                .map((DocumentEntity d) -> CountByKeyResponse.builder()
                        .key(d.getTitle())
                        .value(d.getViewCount() == null ? 0L : d.getViewCount())
                        .build())
                .collect(Collectors.toList());
    }
}