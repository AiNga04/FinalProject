package org.vti.jamie.com.testingsystem.dto.responses.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
