package com.saas.cloud_storage_app.modules.share.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessShareLinkRequest {

    // nullable — link không có password thì không cần gửi
    private String password;
}