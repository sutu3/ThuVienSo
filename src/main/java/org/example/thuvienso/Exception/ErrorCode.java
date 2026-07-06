package org.example.thuvienso.Exception;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@NoArgsConstructor
public enum ErrorCode {
    INVALID_KEY(1001,"Invalid key", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003,"Mật khẩu không hợp lệ", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(1002,"Không tìm thấy vai trò", HttpStatus.NOT_FOUND),
    ROLE_IS_EXIST(1003,"Vai trò đã tồn tại", HttpStatus.CONFLICT),
    FILE_NOT_FOUND(1002,"Không tìm thấy tập tin", HttpStatus.NOT_FOUND),
    FILE_IS_TO_BIG(1004,"Tập tin quá lớn để lưu trữ",HttpStatus.BAD_REQUEST),
    FILE_IS_EXIST(1003,"Tập tin đã tồn tại", HttpStatus.CONFLICT),
    COLLECTION_NOT_FOUND(1002,"Không tìm thấy bộ sưu tập", HttpStatus.NOT_FOUND),
    COLLECTION_IS_EXIST(1003,"Bộ sưu tập đã tồn tại", HttpStatus.CONFLICT),
    DOCUMENT_NOT_FOUND(1002,"Không tìm thấy tài liệu", HttpStatus.NOT_FOUND),
    DOCUMENT_IS_EXIST(1003,"Tài liệu đã tồn tại", HttpStatus.CONFLICT),
    FOLDER_NOT_FOUND(1002,"Không tìm thấy thư mục", HttpStatus.NOT_FOUND),
    FOLDER_IS_EXIST(1003,"Thư mục đã tồn tại", HttpStatus.CONFLICT),
    FILE_NOT_SUPPORTED(1003,"Tập tin không được hỗ trợ", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(1002,"Không tìm thấy thể loại", HttpStatus.NOT_FOUND),
    CATEGORY_IS_EXIST(1003,"Thể loại đã tồn tại", HttpStatus.CONFLICT),
    ACCOUNT_NOT_FOUND(1002,"Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
//    ACCOUNT_IS_EXIST(1003,"Vai trò đã tồn tại", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1004,"Tên đăng nhập hoặc mật khẩu không đúng",HttpStatus.UNAUTHORIZED),
    UNCATEGORIZED(9999,"Uncategorized", HttpStatus.INTERNAL_SERVER_ERROR);

    ErrorCode(int Code,String Message, HttpStatusCode sponse){
        this.code = Code;
        this.message = Message;
        this.status = sponse;
    }
    private int code;
    private String message;
    private HttpStatusCode status;
}
