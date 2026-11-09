package org.vti.jamie.com.testingsystem.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.vti.jamie.com.testingsystem.config.SecurityConfig;
import org.vti.jamie.com.testingsystem.form.account.AccountSearchForm;
import org.vti.jamie.com.testingsystem.form.account.CreateAccountForm;
import org.vti.jamie.com.testingsystem.form.account.UpdateAccountForm;
import org.vti.jamie.com.testingsystem.dto.account.AccountResponse;
import org.vti.jamie.com.testingsystem.form.auth.LoginForm;
import org.vti.jamie.com.testingsystem.form.auth.RefreshTokenForm;
import org.vti.jamie.com.testingsystem.dto.auth.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.exception.GlobalExceptionHandler;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.repository.IRevokedSessionRepository;
import org.vti.jamie.com.testingsystem.service.IAuthService;
import org.vti.jamie.com.testingsystem.service.IAccountService;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import java.util.Optional;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private JwtDecoder decoder;
    private IAccountService service;
    private IAccountRepository repository;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        decoder = context.getBean(JwtDecoder.class);
        service = context.getBean(IAccountService.class);
        when(service.getAccounts(
                any(AccountSearchForm.class),
                any(Pageable.class)
        )).thenReturn(org.springframework.data.domain.Page.empty());
        repository = context.getBean(IAccountRepository.class);
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/api/v1/").contextPath("/api/v1"))
                .apply(springSecurity()).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void loginIsPublicUnderApiContextPath() throws Exception {
        var auth = context.getBean(IAuthService.class);
        var request = new LoginForm("admin", "test-password");
        when(auth.login(request)).thenReturn(new LoginResponse("access", "refresh"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh"));
        verify(auth).login(request);
        verifyNoInteractions(decoder);
    }

    @Test
    void refreshIsPublicUnderApiContextPath() throws Exception {
        var auth = context.getBean(IAuthService.class);
        var request = new RefreshTokenForm("old-refresh");
        when(auth.refresh(request)).thenReturn(new LoginResponse("new-access", "new-refresh"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
        verify(auth).refresh(request);
        verifyNoInteractions(decoder);
    }

    @Test
    void wrongPasswordReturnsApplicationErrorRatherThanSecurityRejection() throws Exception {
        var auth = context.getBean(IAuthService.class);
        var request = new LoginForm("admin", "wrong-password");
        when(auth.login(request)).thenThrow(new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Username hoặc password không chính xác"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.error.code").value(401));
        verify(auth).login(request);
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
        Account account = new Account();
        account.setId(3L);
        account.setAccountType(EN_AccountType.valueOf(accountType));
        when(repository.findById(3L)).thenReturn(Optional.of(account));
        when(decoder.decode(accountType)).thenReturn(Jwt.withTokenValue(accountType)
                .header("alg", "HS256").subject("3")
                .claim("sid", "session-" + accountType)
                .claim("accountType", accountType).build());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AccountController.class, AuthController.class, GlobalExceptionHandler.class})
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
            // This manually created MVC context does not load Spring Boot's YAML configuration.
            var pageable = new PageableHandlerMethodArgumentResolver();
            pageable.setPageParameterName("pageNumber");
            pageable.setSizeParameterName("size");
            pageable.setOneIndexedParameters(true);
            pageable.setFallbackPageable(PageRequest.of(0, 10));
            pageable.setMaxPageSize(100);
            resolvers.add(pageable);
        }

        @Bean IAccountRepository accountRepository() { return mock(IAccountRepository.class); }
        @Bean IAccountService accountService() { return mock(IAccountService.class); }
        @Bean JwtDecoder jwtDecoder() { return mock(JwtDecoder.class); }
        @Bean IRevokedSessionRepository revokedSessions() { return mock(IRevokedSessionRepository.class); }
        @Bean IAuthService authService() { return mock(IAuthService.class); }
    }

    @Test
    void deletedAdminTokenCannotAccessManagementRoute() throws Exception {
        stubToken("ADMIN");
        Account deleted = new Account();
        deleted.setStatus(EN_Status.DELETED);
        deleted.setAccountType(EN_AccountType.ADMIN);
        when(repository.findById(3L)).thenReturn(Optional.of(deleted));
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void restoredAccountCannotReuseTokenFromBeforeDeletion() throws Exception {
        stubToken("ADMIN");
        Account restored = new Account();
        restored.setAccountType(EN_AccountType.ADMIN);
        restored.setTokenVersion(2);
        when(repository.findById(3L)).thenReturn(Optional.of(restored));
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demotedAdminCannotUseStaleRoleClaim() throws Exception {
        stubToken("ADMIN");
        Account employee = new Account();
        employee.setAccountType(EN_AccountType.EMPLOYEE);
        when(repository.findById(3L)).thenReturn(Optional.of(employee));
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminCanUseEveryCrudAndStatusEndpoint() throws Exception {
        String accountBody = "{\"username\":\"new-user\",\"email\":\"user@example.com\","
                + "\"password\":\"test-password\",\"firstName\":\"Test\",\"lastName\":\"User\","
                + "\"accountType\":\"EMPLOYEE\"}";
        for (String type : new String[]{"ADMIN", "MANAGER", "EMPLOYEE"}) {
            stubToken(type);
            var requests = java.util.List.of(
                    get("/api/v1/accounts/7"),
                    post("/api/v1/accounts").content(accountBody),
                    put("/api/v1/accounts/7").content(accountBody.replace(
                            "\"password\":\"test-password\",", "")),
                    patch("/api/v1/accounts/updateStatusList")
                            .content("{\"ids\":[7,8],\"status\":\"ACTIVE\"}"),
                    patch("/api/v1/accounts/7/status").content("{\"status\":\"ACTIVE\"}")
            );
            for (var request : requests) {
                var result = mvc.perform(request.contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + type));
                if (type.equals("ADMIN")) {
                    result.andExpect(status().is2xxSuccessful());
                } else {
                    result.andExpect(status().isForbidden());
                }
            }
        }
    }

    @Test
    void invalidStatusAndEmptyBulkDeleteReturnBadRequest() throws Exception {
        stubToken("ADMIN");
        mvc.perform(patch("/api/v1/accounts/updateStatusList")
                        .header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[7],\"status\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details.status").exists());
        mvc.perform(patch("/api/v1/accounts/updateStatusList")
                        .header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[],\"status\":\"DELETED\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void nonPositivePageSizeFallsBackToDefault() throws Exception {
        stubToken("ADMIN");
        mvc.perform(get("/api/v1/accounts?size=0")
                        .header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
        var paging = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAccounts(any(AccountSearchForm.class), paging.capture());
        org.junit.jupiter.api.Assertions.assertEquals(10, paging.getValue().getPageSize());
    }

    @Test
    void missingPagingUsesFirstPageAndDefaultSize() throws Exception {
        stubToken("ADMIN");
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
        var paging = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAccounts(any(AccountSearchForm.class), paging.capture());
        org.junit.jupiter.api.Assertions.assertEquals(0, paging.getValue().getPageNumber());
        org.junit.jupiter.api.Assertions.assertEquals(10, paging.getValue().getPageSize());
    }

    @Test
    void firstPageIsOneIndexedAndSizeIsCapped() throws Exception {
        stubToken("ADMIN");
        mvc.perform(get("/api/v1/accounts?pageNumber=1&size=101")
                        .header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
        var paging = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAccounts(any(AccountSearchForm.class), paging.capture());
        org.junit.jupiter.api.Assertions.assertEquals(0, paging.getValue().getPageNumber());
        org.junit.jupiter.api.Assertions.assertEquals(100, paging.getValue().getPageSize());
    }

    @Test
    void updateStatusListReturnsUpdatedAccountsInApiResponse() throws Exception {
        stubToken("ADMIN");
        AccountResponse account = new AccountResponse();
        account.setId(7L);
        account.setStatus(EN_Status.DELETED);
        when(service.updateStatusList(java.util.List.of(7L), EN_Status.DELETED, "3"))
                .thenReturn(java.util.List.of(account));
        mvc.perform(patch("/api/v1/accounts/updateStatusList")
                        .header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[7],\"status\":\"DELETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái thành công"))
                .andExpect(jsonPath("$.data[0].id").value(7))
                .andExpect(jsonPath("$.data[0].status").value("DELETED"));
    }

    @Test
    void createAcceptsBodyWithoutAccountTypeAndDepartment() throws Exception {
        stubToken("ADMIN");
        mvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new-user\",\"email\":\"user@example.com\","
                                + "\"password\":\"test-password\",\"firstName\":\"Test\",\"lastName\":\"User\"}"))
                .andExpect(status().isCreated());
        var request = org.mockito.ArgumentCaptor.forClass(
                CreateAccountForm.class);
        verify(service).create(request.capture(), eq("3"));
        org.junit.jupiter.api.Assertions.assertEquals(EN_AccountType.EMPLOYEE,
                request.getValue().getAccountType());
        org.junit.jupiter.api.Assertions.assertNull(request.getValue().getDepartmentId());
    }

    @Test
    void updateAcceptsOneFieldAndTracksExplicitNullDepartment() throws Exception {
        stubToken("ADMIN");
        mvc.perform(put("/api/v1/accounts/7").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"firstName\":\"New\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/accounts/7").header("Authorization", "Bearer ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"departmentId\":null}"))
                .andExpect(status().isOk());
        var request = org.mockito.ArgumentCaptor.forClass(
                UpdateAccountForm.class);
        verify(service, times(2)).update(eq(7L), request.capture(), eq("3"));
        org.junit.jupiter.api.Assertions.assertFalse(request.getAllValues().get(0).isDepartmentIdPresent());
        org.junit.jupiter.api.Assertions.assertTrue(request.getAllValues().get(1).isDepartmentIdPresent());
    }

    @Test
    void updateRejectsBlankInvalidAndExplicitNullRequiredValues() throws Exception {
        stubToken("ADMIN");
        for (String body : new String[]{"{\"firstName\":\" \"}", "{\"email\":\"invalid\"}",
                "{\"username\":null}", "{\"accountType\":null}", "{\"departmentId\":0}"}) {
            mvc.perform(put("/api/v1/accounts/7").header("Authorization", "Bearer ADMIN")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void listBindsFiltersFromQueryParametersWithoutBody() throws Exception {
        stubToken("ADMIN");
        mvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer ADMIN")
                        .param("pageNumber", "2").param("size", "20")
                        .param("search", "Nga").param("accountType", "EMPLOYEE")
                        .param("departmentId", "7").param("status", "ACTIVE"))
                .andExpect(status().isOk());
        var request = org.mockito.ArgumentCaptor.forClass(
                AccountSearchForm.class);
        var paging = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAccounts(request.capture(), paging.capture());
        var filters = request.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(1, paging.getValue().getPageNumber());
        org.junit.jupiter.api.Assertions.assertEquals(20, paging.getValue().getPageSize());
        org.junit.jupiter.api.Assertions.assertEquals("Nga", filters.getSearch());
        org.junit.jupiter.api.Assertions.assertEquals(EN_AccountType.EMPLOYEE, filters.getAccountType());
        org.junit.jupiter.api.Assertions.assertEquals(7L, filters.getDepartmentId());
        org.junit.jupiter.api.Assertions.assertEquals(EN_Status.ACTIVE, filters.getStatus());
    }

    @Test
    void listReturnsArrayWithFilteredTotalInData2nd() throws Exception {
        stubToken("ADMIN");
        AccountResponse account = new AccountResponse();
        account.setId(7L);
        account.setUsername("employee01");
        when(service.getAccounts(any(AccountSearchForm.class), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(account), org.springframework.data.domain.PageRequest.of(0, 10), 16));

        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(7))
                .andExpect(jsonPath("$.data2nd").value(16))
                .andExpect(jsonPath("$.data.content").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(200));
    }

    @Test
    void emptyPageReturnsEmptyArrayAndStillPreservesFilteredTotal() throws Exception {
        stubToken("ADMIN");
        when(service.getAccounts(
                any(AccountSearchForm.class),
                any(Pageable.class)
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(), org.springframework.data.domain.PageRequest.of(2, 10), 16));

        mvc.perform(get("/api/v1/accounts?pageNumber=3").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.data2nd").value(16));
        var paging = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(service).getAccounts(any(AccountSearchForm.class), paging.capture());
        org.junit.jupiter.api.Assertions.assertEquals(2, paging.getValue().getPageNumber());
    }

    @Test
    void updateSingleStatusReturnsObjectWithNullData2nd() throws Exception {
        stubToken("ADMIN");
        for (EN_Status state : EN_Status.values()) {
            AccountResponse account = new AccountResponse();
            account.setId(7L);
            account.setStatus(state);
            when(service.updateStatus(7L, state, "3")).thenReturn(account);

            mvc.perform(patch("/api/v1/accounts/7/status")
                            .header("Authorization", "Bearer ADMIN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + state.name() + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(1))
                    .andExpect(jsonPath("$.data.id").value(7))
                    .andExpect(jsonPath("$.data.status").value(state.name()))
                    .andExpect(jsonPath("$.data2nd").value(org.hamcrest.Matchers.nullValue()));
        }
    }

    @Test
    void updateSingleStatusRejectsMissingOrUnknownStatus() throws Exception {
        stubToken("ADMIN");
        for (String body : new String[]{"{}", "{\"status\":null}", "{\"status\":\"UNKNOWN\"}"}) {
            mvc.perform(patch("/api/v1/accounts/7/status")
                            .header("Authorization", "Bearer ADMIN")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void everyAccountTypeCanLogoutWithoutRequestBody() throws Exception {
        for (String type : new String[]{"ADMIN", "MANAGER", "EMPLOYEE"}) {
            stubToken(type);
            mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + type))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(1))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data2nd").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.error.message").value("Đăng xuất thành công"));
        }
        verify(context.getBean(IAuthService.class), times(3)).logout(any(Jwt.class));
    }

    @Test
    void logoutRequiresValidAccessToken() throws Exception {
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
        verifyNoInteractions(context.getBean(IAuthService.class));
    }

    @Test
    void loggedOutSessionIsBlockedButAnotherSessionForSameAccountStillWorks() throws Exception {
        stubToken("ADMIN");
        var revoked = context.getBean(IRevokedSessionRepository.class);
        doAnswer(call -> {
            when(revoked.existsById("session-ADMIN")).thenReturn(true);
            return null;
        }).when(context.getBean(IAuthService.class)).logout(any(Jwt.class));
        when(decoder.decode("second-token")).thenReturn(Jwt.withTokenValue("second-token")
                .header("alg", "HS256").subject("3").claim("sid", "other-session")
                .claim("accountType", "ADMIN").build());

        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/accounts/me").header("Authorization", "Bearer ADMIN"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/accounts/me").header("Authorization", "Bearer second-token"))
                .andExpect(status().isOk());
    }

    @Test
    void accessTokensWithoutSessionMustLoginAgain() throws Exception {
        stubToken("ADMIN");
        when(decoder.decode("legacy")).thenReturn(Jwt.withTokenValue("legacy")
                .header("alg", "HS256").subject("3").claim("accountType", "ADMIN").build());
        mvc.perform(get("/api/v1/accounts/me").header("Authorization", "Bearer legacy"))
                .andExpect(status().isUnauthorized());
    }
}
