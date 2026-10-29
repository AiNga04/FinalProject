package org.vti.jamie.com.testingsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.dto.requests.RefreshTokenRequest;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;
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
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void refreshReturnsApiResponseWithNewPair() throws Exception {
        when(service.refresh(new RefreshTokenRequest("old-refresh")))
                .thenReturn(new LoginResponse("new-access", "new-refresh"));
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"))
                .andExpect(jsonPath("$.error.code").value(200));
    }

    @Test
    void blankRefreshTokenReturnsValidationError() throws Exception {
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.error.details.refreshToken").exists());
        verifyNoInteractions(service);
    }

    @Test
    void invalidRefreshTokenReturnsUnauthorizedApiResponse() throws Exception {
        when(service.refresh(new RefreshTokenRequest("invalid")))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Refresh token không hợp lệ hoặc đã hết hạn"));
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.error.code").value(401));
    }
}
