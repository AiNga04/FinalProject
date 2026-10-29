package org.vti.jamie.com.testingsystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.dto.requests.LoginRequest;
import org.vti.jamie.com.testingsystem.dto.requests.RefreshTokenRequest;
import org.vti.jamie.com.testingsystem.dto.responses.ApiResponse;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.RefreshToken;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private IAccountRepository accountRepository;
    @Mock private JwtEncoder jwtEncoder;
    @Mock private RefreshTokenService refreshTokenService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(authenticationManager, accountRepository,
                jwtEncoder, refreshTokenService);
        ReflectionTestUtils.setField(service, "issuer", "testing-system");
        ReflectionTestUtils.setField(service, "accessTokenMinutes", 15L);
    }

    @Test
    void successfulLoginReturnsOnlyTwoTokensInResponseData() {
        Account account = account(EN_Status.ACTIVE);
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(account));
        when(jwtEncoder.encode(any())).thenReturn(Jwt.withTokenValue("access-token")
                .header("alg", "HS256").subject("3").build());
        when(refreshTokenService.create(eq(account), anyString())).thenReturn("refresh-token");

        var result = service.login(new LoginRequest("admin", "test-password"));

        var mapper = new JsonMapper();
        var json = mapper.readTree(mapper.writeValueAsString(
                ApiResponse.success(200, "Đăng nhập thành công", result)));
        assertEquals(1, json.path("result").asInt());
        assertTrue(json.path("time").asLong() > 0);
        assertEquals("", json.path("dataDescription").asText());
        assertTrue(json.path("data2nd").isNull());
        assertEquals(2, json.path("data").size());
        assertEquals("access-token", json.path("data").path("accessToken").asText());
        assertEquals("refresh-token", json.path("data").path("refreshToken").asText());
        assertEquals(200, json.path("error").path("code").asInt());
        assertFalse(json.path("error").has("details"));

        var parameters = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parameters.capture());
        var claims = parameters.getValue().getClaims();
        assertEquals("3", claims.getSubject());
        assertNotNull(claims.getClaim("sid"));
        assertEquals(900, claims.getExpiresAt().getEpochSecond()
                - claims.getIssuedAt().getEpochSecond());
    }

    @Test
    void wrongPasswordDoesNotIssueTokens() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("admin", "wrong-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(accountRepository, jwtEncoder, refreshTokenService);
    }

    @Test
    void inactiveAccountDoesNotIssueTokens() {
        when(accountRepository.findByUsername("admin"))
                .thenReturn(Optional.of(account(EN_Status.INACTIVE)));

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("admin", "test-password")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(jwtEncoder, refreshTokenService);
    }

    @Test
    void refreshIssuesNewPairUsingCurrentAccountWithoutPasswordAuthentication() {
        Account account = account(EN_Status.ACTIVE);
        RefreshToken token = new RefreshToken();
        token.setAccount(account);
        token.setSessionId("same-session");
        when(refreshTokenService.consume("old-refresh")).thenReturn(token);
        when(jwtEncoder.encode(any())).thenReturn(Jwt.withTokenValue("new-access")
                .header("alg", "HS256").subject("3").build());
        when(refreshTokenService.create(account, "same-session")).thenReturn("new-refresh");

        var response = service.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        verifyNoInteractions(authenticationManager, accountRepository);
        var order = inOrder(refreshTokenService);
        order.verify(refreshTokenService).consume("old-refresh");
        order.verify(refreshTokenService).create(account, "same-session");
        var parameters = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parameters.capture());
        assertEquals("same-session", parameters.getValue().getClaims().getClaim("sid"));
    }

    @Test
    void rejectedRefreshTokenDoesNotIssueAnyTokens() {
        when(refreshTokenService.consume("invalid"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        assertThrows(ResponseStatusException.class,
                () -> service.refresh(new RefreshTokenRequest("invalid")));

        verifyNoInteractions(jwtEncoder, authenticationManager, accountRepository);
        verify(refreshTokenService, never()).create(any(), any());
    }

    @Test
    void logoutRevokesSessionFromAuthenticatedToken() {
        Jwt jwt = Jwt.withTokenValue("access-token").header("alg", "HS256")
                .subject("3").claim("sid", "current-session").build();
        service.logout(jwt);
        verify(refreshTokenService).revokeSession("current-session", 3L);
        verifyNoInteractions(jwtEncoder, authenticationManager, accountRepository);
    }

    private Account account(EN_Status status) {
        Account account = new Account();
        account.setId(3L);
        account.setUsername("admin");
        account.setAccountType(EN_AccountType.ADMIN);
        account.setStatus(status);
        return account;
    }
}
