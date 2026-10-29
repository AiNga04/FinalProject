package org.vti.jamie.com.testingsystem.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.vti.jamie.com.testingsystem.config.SecurityConfig;
import org.vti.jamie.com.testingsystem.dto.responses.AccountResponse;
import org.vti.jamie.com.testingsystem.exception.GlobalExceptionHandler;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.service.IAccountService;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private JwtDecoder decoder;
    private IAccountService service;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        decoder = context.getBean(JwtDecoder.class);
        service = context.getBean(IAccountService.class);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void everyAccountTypeCanAccessOwnProfile() throws Exception {
        AccountResponse profile = new AccountResponse();
        profile.setId(3L);
        profile.setUsername("database-name");
        when(service.getMe("3")).thenReturn(profile);
        for (String type : new String[]{"ADMIN", "MANAGER", "EMPLOYEE"}) {
            stubToken(type);
            mvc.perform(get("/api/v1/accounts/me").header("Authorization", "Bearer " + type))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(1))
                    .andExpect(jsonPath("$.data.id").value(3))
                    .andExpect(jsonPath("$.data.username").value("database-name"));
        }
    }

    @Test
    void missingTokenReturnsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/accounts/me")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        when(decoder.decode("invalid")).thenThrow(new BadJwtException("Invalid token"));
        mvc.perform(get("/api/v1/accounts/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void adminAuthorityCanAccessManagementRoute() throws Exception {
        stubToken("ADMIN");
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void employeeAndManagerCannotAccessManagementRoute() throws Exception {
        for (String type : new String[]{"EMPLOYEE", "MANAGER"}) {
            stubToken(type);
            mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer " + type))
                    .andExpect(status().isForbidden());
        }
    }

    private void stubToken(String accountType) {
        when(decoder.decode(accountType)).thenReturn(Jwt.withTokenValue(accountType)
                .header("alg", "HS256").subject("3")
                .claim("accountType", accountType).build());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AccountController.class, GlobalExceptionHandler.class,
            ManagementTestController.class})
    static class TestConfig {
        @Bean IAccountRepository accountRepository() { return mock(IAccountRepository.class); }
        @Bean IAccountService accountService() { return mock(IAccountService.class); }
        @Bean JwtDecoder jwtDecoder() { return mock(JwtDecoder.class); }
    }

    // Route chỉ dùng trong test để kiểm tra quy tắc ADMIN của filter chain thực tế.
    @RestController
    static class ManagementTestController {
        @GetMapping("/api/v1/accounts")
        public String accounts() { return "ok"; }
    }
}
