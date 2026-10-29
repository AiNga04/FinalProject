package org.vti.jamie.com.testingsystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.RefreshToken;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.vti.jamie.com.testingsystem.repository.IRevokedSessionRepository;
import org.vti.jamie.com.testingsystem.entity.RevokedSession;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private IRefreshTokenRepository repository;
    @Mock private IRevokedSessionRepository revokedSessions;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, revokedSessions);
        ReflectionTestUtils.setField(service, "refreshTokenDays", 7L);
    }

    @Test
    void storesHashInsteadOfRawTokenWithAccountAndExpiry() throws Exception {
        Account account = new Account();
        Instant before = Instant.now();

        String rawToken = service.create(account, "session-id");

        var saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        RefreshToken token = saved.getValue();
        assertSame(account, token.getAccount());
        assertEquals("session-id", token.getSessionId());
        assertEquals(32, Base64.getDecoder().decode(rawToken).length);
        assertNotEquals(rawToken, token.getTokenHash());
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8))), token.getTokenHash());
        assertFalse(token.getCreatedAt().isBefore(before));
        assertEquals(Duration.ofDays(7), Duration.between(
                token.getCreatedAt(), token.getExpiresAt()));
        assertNull(token.getRevokedAt());
    }

    @Test
    void separateLoginsGetDifferentRefreshTokens() {
        Account account = new Account();
        assertNotEquals(service.create(account, "session-id"), service.create(account, "session-id"));
    }

    @Test
    void invalidLifetimeDoesNotSaveToken() {
        ReflectionTestUtils.setField(service, "refreshTokenDays", 0L);
        assertThrows(IllegalStateException.class, () -> service.create(new Account(), "session-id"));
        verifyNoInteractions(repository);
    }

    @Test
    void validTokenIsConsumedAndCannotBeReused() throws Exception {
        RefreshToken token = usableToken();
        when(repository.findByTokenHashForUpdate(hash("raw-token")))
                .thenReturn(Optional.of(token));

        assertSame(token, service.consume("raw-token"));
        assertNotNull(token.getRevokedAt());
        verify(repository).save(token);

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.consume("raw-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(repository, times(1)).save(token);
    }

    @Test
    void unknownTokenIsRejected() throws Exception {
        when(repository.findByTokenHashForUpdate(hash("unknown")))
                .thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.consume("unknown"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        RefreshToken token = usableToken();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        when(repository.findByTokenHashForUpdate(hash("expired")))
                .thenReturn(Optional.of(token));
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.consume("expired"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertNull(token.getRevokedAt());
        verify(repository, never()).save(any());
    }

    @Test
    void inactiveAccountCannotRefresh() throws Exception {
        RefreshToken token = usableToken();
        token.getAccount().setStatus(EN_Status.INACTIVE);
        when(repository.findByTokenHashForUpdate(hash("inactive")))
                .thenReturn(Optional.of(token));
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.consume("inactive"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void blankTokenIsRejectedWithoutDatabaseQuery() {
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.consume(" "));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(repository);
    }

    private RefreshToken usableToken() {
        Account account = new Account();
        account.setStatus(EN_Status.ACTIVE);
        RefreshToken token = new RefreshToken();
        token.setAccount(account);
        token.setSessionId("session-id");
        token.setExpiresAt(Instant.now().plusSeconds(60));
        return token;
    }

    private String hash(String token) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void logoutPersistsSessionRevocationAndRevokesOnlyThatSession() {
        service.revokeSession("session-id", 3L);

        var revoked = ArgumentCaptor.forClass(RevokedSession.class);
        verify(revokedSessions).saveAndFlush(revoked.capture());
        assertEquals("session-id", revoked.getValue().getSessionId());
        assertEquals(3L, revoked.getValue().getAccountId());
        assertNotNull(revoked.getValue().getRevokedAt());
        verify(repository).revokeBySession(eq("session-id"), eq(3L), any());
        verify(repository, never()).revokeByAccountIds(any(), any());
    }

    @Test
    void loggedOutSessionCannotConsumeRefreshToken() throws Exception {
        when(repository.findByTokenHashForUpdate(hash("raw-token")))
                .thenReturn(Optional.of(usableToken()));
        when(revokedSessions.existsById("session-id")).thenReturn(true);
        var exception = assertThrows(ResponseStatusException.class, () -> service.consume("raw-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void loggedOutSessionCannotIssueNewRefreshTokens() {
        when(revokedSessions.existsById("session-id")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> service.create(new Account(), "session-id"));
        verify(repository, never()).save(any());
    }

    @Test
    void legacyRefreshTokenWithoutSessionMustLoginAgain() throws Exception {
        RefreshToken token = usableToken();
        token.setSessionId(null);
        when(repository.findByTokenHashForUpdate(hash("legacy"))).thenReturn(Optional.of(token));
        assertThrows(ResponseStatusException.class, () -> service.consume("legacy"));
        verify(repository, never()).save(any());
    }
}
