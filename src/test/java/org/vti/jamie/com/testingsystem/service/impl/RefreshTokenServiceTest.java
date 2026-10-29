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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private IRefreshTokenRepository repository;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository);
        ReflectionTestUtils.setField(service, "refreshTokenDays", 7L);
    }

    @Test
    void storesHashInsteadOfRawTokenWithAccountAndExpiry() throws Exception {
        Account account = new Account();
        Instant before = Instant.now();

        String rawToken = service.create(account);

        var saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        RefreshToken token = saved.getValue();
        assertSame(account, token.getAccount());
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
        assertNotEquals(service.create(account), service.create(account));
    }

    @Test
    void invalidLifetimeDoesNotSaveToken() {
        ReflectionTestUtils.setField(service, "refreshTokenDays", 0L);
        assertThrows(IllegalStateException.class, () -> service.create(new Account()));
        verifyNoInteractions(repository);
    }
}
