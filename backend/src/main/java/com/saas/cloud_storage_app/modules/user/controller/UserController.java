package com.saas.cloud_storage_app.modules.user.controller;


import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    

}

