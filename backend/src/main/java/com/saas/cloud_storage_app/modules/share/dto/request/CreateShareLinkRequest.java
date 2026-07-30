package com.saas.cloud_storage_app.modules.share.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateShareLinkRequest {

    // (1) Password bảo vệ — optional, null = không cần password
    @Size(min = 4, max = 50, message = "Password phải từ 4 đến 50 ký tự")
    private String password;

    // (2) Thời điểm hết hạn — phải trong tương lai
    @Future(message = "Thời gian hết hạn phải trong tương lai")
    private LocalDateTime expiresAt;

    // (3) Giới hạn lượt tải — null = không giới hạn
    @Min(value = 1, message = "Số lượt tải tối thiểu là 1")
    private Integer maxDownloads;
}