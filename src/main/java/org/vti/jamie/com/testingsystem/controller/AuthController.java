package org.vti.jamie.com.testingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.form.auth.LoginForm;
import org.vti.jamie.com.testingsystem.form.auth.RefreshTokenForm;
import org.vti.jamie.com.testingsystem.dto.ApiResponse;
import org.vti.jamie.com.testingsystem.dto.auth.LoginResponse;
import org.vti.jamie.com.testingsystem.service.IAuthService;

import java.time.Duration;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng username và password, trả về accessToken và lưu refreshToken vào HttpOnly Cookie")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginForm request
    ) {
        LoginResponse response = authService.login(request);
        ResponseCookie cookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới token bằng HttpOnly cookie hoặc request body")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieToken,
            @Valid @RequestBody(required = false) RefreshTokenForm body
    ) {
        String token = (cookieToken != null && !cookieToken.isBlank())
                ? cookieToken
                : (body != null ? body.refreshToken() : null);

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy refresh token");
        }

        LoginResponse response = authService.refresh(new RefreshTokenForm(token));
        ResponseCookie cookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất phiên hiện tại và xóa HttpOnly cookie")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            authService.logout(jwt);
        }
        ResponseCookie cookie = buildRefreshTokenCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    private ResponseCookie buildRefreshTokenCookie(String token, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // để chạy được trên localhost http://
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
