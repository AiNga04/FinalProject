package org.vti.jamie.com.testingsystem.service;

import org.vti.jamie.com.testingsystem.form.auth.LoginForm;
import org.vti.jamie.com.testingsystem.form.auth.RefreshTokenForm;
import org.vti.jamie.com.testingsystem.dto.auth.LoginResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface IAuthService {
    LoginResponse login(LoginForm request);

    LoginResponse refresh(RefreshTokenForm request);

    void logout(Jwt jwt);
}
