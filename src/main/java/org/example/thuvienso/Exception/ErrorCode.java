package org.example.thuvienso.Exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@NoArgsConstructor
public enum ErrorCode {
    // ===== Chung (9xxx / 1xxx) =====
    UNCATEGORIZED(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Khóa/tham số không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1003, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),

    // ===== Account / Role (2xxx) =====
    ACCOUNT_NOT_FOUND(2001, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
    ACCOUNT_IS_EXIST(2002, "Tài khoản đã tồn tại", HttpStatus.CONFLICT),
    PASSWORD_INVALID(2003, "Mật khẩu không hợp lệ", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(2004, "Không tìm thấy vai trò", HttpStatus.NOT_FOUND),
    ROLE_IS_EXIST(2005, "Vai trò đã tồn tại", HttpStatus.CONFLICT),

    // ===== Document / Folder / Category (3xxx) =====
    DOCUMENT_NOT_FOUND(3001, "Không tìm thấy tài liệu", HttpStatus.NOT_FOUND),
    DOCUMENT_IS_EXIST(3002, "Tài liệu đã tồn tại", HttpStatus.CONFLICT),
    FOLDER_NOT_FOUND(3003, "Không tìm thấy thư mục", HttpStatus.NOT_FOUND),
    FOLDER_IS_EXIST(3004, "Thư mục đã tồn tại", HttpStatus.CONFLICT),
    FOLDER_NOT_DELETED(4007, "Thư mục chưa được xóa mềm, không thể xóa cứng", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(3005, "Không tìm thấy Danh mục", HttpStatus.NOT_FOUND),
    CATEGORY_IS_EXIST(3006, "Danh mục đã tồn tại", HttpStatus.CONFLICT),

    // ===== File / Collection (4xxx) =====
    FILE_NOT_FOUND(4001, "Không tìm thấy tập tin", HttpStatus.NOT_FOUND),
    FILE_IS_EXIST(4002, "Tập tin đã tồn tại", HttpStatus.CONFLICT),
    FILE_IS_TO_BIG(4003, "Tập tin quá lớn để lưu trữ", HttpStatus.BAD_REQUEST),
    FILE_NOT_SUPPORTED(4004, "Tập tin không được hỗ trợ", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    COLLECTION_NOT_FOUND(4005, "Không tìm thấy bộ sưu tập", HttpStatus.NOT_FOUND),
    COLLECTION_IS_EXIST(4006, "Bộ sưu tập đã tồn tại", HttpStatus.CONFLICT),

    // ===== Book / Borrow (5xxx) =====
    BOOK_NOT_FOUND(5001, "Không tìm thấy sách", HttpStatus.NOT_FOUND),
    BOOK_IS_EXIST(4002, "Sách đã tồn tại", HttpStatus.CONFLICT),
    BOOK_OUT_OF_STOCK(5003, "Sách đã hết bản để mượn", HttpStatus.CONFLICT),
    BORROW_NOT_FOUND(5004, "Không tìm thấy phiếu mượn", HttpStatus.NOT_FOUND),
    BORROW_INVALID_STATUS(5005, "Trạng thái phiếu mượn không hợp lệ cho thao tác này", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    private int code;
    private String message;
    private HttpStatusCode status;
}