package org.vti.jamie.com.testingsystem.service;

import org.vti.jamie.com.testingsystem.dto.requests.LoginRequest;
import org.vti.jamie.com.testingsystem.dto.requests.RefreshTokenRequest;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshTokenRequest request);
    void logout(Jwt jwt);
}
