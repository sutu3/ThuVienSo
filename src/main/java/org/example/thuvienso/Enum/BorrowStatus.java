package org.example.thuvienso.Enum;

public enum BorrowStatus {
    PENDING,    // đăng ký, chờ duyệt
    APPROVED,   // đã duyệt, chờ nhận sách
    BORROWED,   // đang mượn
    RETURNED,   // đã trả
    REJECTED,   // bị từ chối
    OVERDUE     // quá hạn
}