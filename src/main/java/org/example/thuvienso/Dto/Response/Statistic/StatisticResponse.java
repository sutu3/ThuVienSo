package org.example.thuvienso.Dto.Response.Statistic;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticResponse {
    // Tài liệu số
    long totalDocuments;        // tổng tài liệu (chưa xóa)
    long pendingDocuments;      // chờ duyệt
    long approvedDocuments;     // đã duyệt
    long totalViews;            // tổng lượt xem
    long totalDownloads;        // tổng lượt tải
    long totalFiles;            // tổng file trên MinIO (đã lưu DB)

    long totalBooks;            // tổng đầu sách
    long totalBookCopies;       // tổng số bản in
    long availableBookCopies;   // số bản còn cho mượn

    long totalBorrows;          // tổng lượt mượn
    long borrowingCount;        // đang mượn
    long overdueCount;          // quá hạn
    long returnedCount;         // đã trả
}