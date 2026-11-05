package org.vti.jamie.com.testingsystem.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
