package org.vti.jamie.com.testingsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.form.auth.RefreshTokenForm;
import org.vti.jamie.com.testingsystem.dto.auth.LoginResponse;
import org.vti.jamie.com.testingsystem.exception.GlobalExceptionHandler;
import org.vti.jamie.com.testingsystem.service.IAuthService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {
    private IAuthService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(IAuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
                .defaultRequest(post("/api/v1/").contextPath("/api/v1"))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void refreshReturnsApiResponseWithNewPair() throws Exception {
        when(service.refresh(new RefreshTokenForm("old-refresh")))
                .thenReturn(new LoginResponse("new-access", "new-refresh"));
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Làm mới token thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void blankRefreshTokenReturnsValidationError() throws Exception {
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                .andExpect(jsonPath("$.errors.refreshToken").exists());
        verifyNoInteractions(service);
    }

    @Test
    void invalidRefreshTokenReturnsUnauthorizedApiResponse() throws Exception {
        when(service.refresh(new RefreshTokenForm("invalid")))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Refresh token không hợp lệ hoặc đã hết hạn"));
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token không hợp lệ hoặc đã hết hạn"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
