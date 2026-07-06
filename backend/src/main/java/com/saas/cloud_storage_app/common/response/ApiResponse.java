package com.saas.cloud_storage_app.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResponse<T>{
    private int status;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    //trả về kết quả thành công + data
    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .status(200)
                .message("success")
                .data(data)
                .build();
    }
    //trả về kết quả thành công message + data
    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }
    //trả về thành công không có data
    public static <T> ApiResponse<T> success(String message){
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .build();
    }
    // trả về lỗi
    public static <T> ApiResponse<T> error(int status,String message){
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .build();
    }

}
