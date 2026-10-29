package org.vti.jamie.com.testingsystem.dto.responses;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
