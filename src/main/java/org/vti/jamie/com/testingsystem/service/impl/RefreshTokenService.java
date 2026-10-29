package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.RefreshToken;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-token-days:7}")
    private long refreshTokenDays;

    @Transactional
    public String create(Account account) {
        if (refreshTokenDays <= 0) {
            throw new IllegalStateException("Refresh token lifetime must be positive");
        }

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getEncoder().encodeToString(randomBytes);

        Instant now = Instant.now();
        RefreshToken token = new RefreshToken();
        token.setAccount(account);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(token);

        return rawToken;
    }

    // Gọi trong transaction của AuthService.refresh để thu hồi và cấp mới nguyên tử.
    @Transactional
    public Account consume(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 512) {
            throw invalidToken();
        }

        RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(this::invalidToken);
        Instant now = Instant.now();
        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw invalidToken();
        }

        Account account = token.getAccount();
        if (account.getStatus() != EN_Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tài khoản chưa được kích hoạt hoặc đã bị khóa");
        }

        token.setRevokedAt(now);
        refreshTokenRepository.save(token);
        return account;
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Refresh token không hợp lệ hoặc đã hết hạn");
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
