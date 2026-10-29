package org.vti.jamie.com.testingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.vti.jamie.com.testingsystem.dto.requests.LoginRequest;
import org.vti.jamie.com.testingsystem.dto.requests.RefreshTokenRequest;
import org.vti.jamie.com.testingsystem.dto.responses.ApiResponse;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;
import org.vti.jamie.com.testingsystem.service.IAuthService;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng username và password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Đăng nhập thành công",
                        response
                )
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cấp cặp token mới bằng refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(
                200, "Làm mới token thành công", response
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất phiên hiện tại")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt);
        return ResponseEntity.ok(ApiResponse.success(200, "Đăng xuất thành công", null));
    }
}
