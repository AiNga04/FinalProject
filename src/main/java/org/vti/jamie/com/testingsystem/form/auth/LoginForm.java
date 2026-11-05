package org.vti.jamie.com.testingsystem.form.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginForm(
        @NotBlank(message = "Username không được để trống")
        String username,

        @NotBlank(message = "Password không được để trống")
        String password
){}