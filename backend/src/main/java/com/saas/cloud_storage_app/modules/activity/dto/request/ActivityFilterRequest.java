package com.saas.cloud_storage_app.modules.activity.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActivityFilterRequest {

    private String action;      // lọc theo action type
    private String targetType;  // lọc theo "FILE", "FOLDER", "MEMBER"

    // (1) Lọc theo khoảng thời gian
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;


    private int page = 0;


    private int size = 20;
}