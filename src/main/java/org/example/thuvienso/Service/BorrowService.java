package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.BorrowRequest;
import org.example.thuvienso.Dto.Response.Borrow.BorrowResponse;

import java.util.List;

public interface BorrowService {
    BorrowResponse register(BorrowRequest request); // người dùng đăng ký mượn
    BorrowResponse approve(String idBorrow);         // admin/thủ thư duyệt
    BorrowResponse reject(String idBorrow);          // admin/thủ thư từ chối
    BorrowResponse markBorrowed(String idBorrow);    // xác nhận đã giao sách
    BorrowResponse returnBook(String idBorrow);      // trả sách
    List<BorrowResponse> getAll();
    List<BorrowResponse> getMyBorrows(String idAccount);
    BorrowResponse getById(String idBorrow);
}