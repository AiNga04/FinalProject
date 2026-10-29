package org.vti.jamie.com.testingsystem.service;

import org.springframework.data.domain.Page;
import org.vti.jamie.com.testingsystem.dto.requests.LoginRequest;
import org.vti.jamie.com.testingsystem.dto.responses.AccountResponse;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    Page<AccountResponse> getAccounts(int page, int size);
}
