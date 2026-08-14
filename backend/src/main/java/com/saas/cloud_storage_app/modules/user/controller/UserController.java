package com.saas.cloud_storage_app.modules.user.controller;


import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.user.dto.request.ChangePasswordRequest;
import com.saas.cloud_storage_app.modules.user.dto.request.UpdateProfileRequest;
import com.saas.cloud_storage_app.modules.user.dto.response.StorageResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý thông tin cá nhân")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    //laay email tu ten
    public String getEmail(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    //xem profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
                UserResponse response  = userService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //xem dung luong
    @GetMapping("/me/storage")
    public ResponseEntity<ApiResponse<StorageResponse>> getMyStorage(
        @AuthenticationPrincipal UserDetails userDetails
    ){
        StorageResponse response = userService.getStorageInfo(getEmail(userDetails));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //upload avatar

    @PostMapping(value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //  upload binary data
    public ResponseEntity<ApiResponse<UserResponse>> getMyAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file
    ){
        UserResponse response = userService.uploadAvatar(getEmail(userDetails), file);
        return ResponseEntity.ok(ApiResponse.success(response,"Upload thành công"));
    }

    //đổi mat khau
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ){
        userService.changePassword(getEmail(userDetails),request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));

    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(
                getEmail(userDetails), request
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật thành công"));
    }



}

