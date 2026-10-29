package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token không được để trống")
        @Size(max = 512, message = "Refresh token quá dài")
        String refreshToken
) {
}
