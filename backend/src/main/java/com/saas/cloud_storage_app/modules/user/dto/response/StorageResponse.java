package com.saas.cloud_storage_app.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StorageResponse {

    //Response riêng cho endpoint xem dung lượng
    private Long usedBytes;
    private Long limitBytes;
    private Long availableBytes;    // limitBytes - usedBytes
    private String usedFormatted;
    private String limitFormatted;
    private String availableFormatted;
    private Double usedPercent;
}