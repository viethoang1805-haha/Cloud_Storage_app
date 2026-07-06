package com.saas.cloud_storage_app.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    //định nghĩa mã lỗi cho auth
    INVALID_CREDENTIALS(401, "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(401, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "Bạn chưa đăng nhập", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(403, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),

    //định nghĩa user
    USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(409, "Email đã được sử dụng", HttpStatus.CONFLICT),
    WRONG_PASSWORD(400, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),

    //định nghĩa member
    MEMBER_NOT_FOUND(404, "Không tìm thấy thành viên", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_EXISTS(409, "Thành viên đã tồn tại trong workspace", HttpStatus.CONFLICT),

    //định nghĩa workspace
    WORKSPACE_NOT_FOUND(404, "Không tìm thấy workspace", HttpStatus.NOT_FOUND),
    WORKSPACE_ACCESS_DENIED(403, "Bạn không có quyền truy cập workspace này", HttpStatus.FORBIDDEN),

    // folder
    FOLDER_NOT_FOUND(404, "Không tìm thấy thư mục", HttpStatus.NOT_FOUND),

    //file
    FILE_NOT_FOUND(404, "Không tìm thấy file", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(500, "Upload file thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(500, "Xóa file thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    //share
    SHARE_LINK_NOT_FOUND(404, "Không tìm thấy link chia sẻ", HttpStatus.NOT_FOUND),
    SHARE_LINK_EXPIRED(410, "Link chia sẻ đã hết hạn", HttpStatus.GONE),

    //common
    VALIDATION_FAILED(400, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(500, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);


    private int code;
    private String message;
    private HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;

    }
}
