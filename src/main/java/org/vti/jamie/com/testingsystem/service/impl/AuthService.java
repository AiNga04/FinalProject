package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.dto.requests.LoginRequest;
import org.vti.jamie.com.testingsystem.dto.requests.RefreshTokenRequest;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.service.IAuthService;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final IAccountRepository accountRepository;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-minutes}")
    private long accessTokenMinutes;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 1. Kiểm tra username và password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Username hoặc password không chính xác"
            );
        }

        // 2. Lấy thông tin tài khoản đã xác thực
        var account = accountRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Username hoặc password không chính xác"
                ));

        // 3. Kiểm tra trạng thái tài khoản
        if (account.getStatus() != EN_Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản chưa được kích hoạt hoặc đã bị khóa"
            );
        }

        return issueTokens(account);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        Account account = refreshTokenService.consume(request.refreshToken());
        return issueTokens(account);
    }

    private LoginResponse issueTokens(Account account) {
        // Dùng chung cho login và refresh, luôn lấy quyền hiện tại từ database.
        Instant now = Instant.now();

        Instant expiresAt = now.plus(
                accessTokenMinutes,
                ChronoUnit.MINUTES
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(account.getId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("username", account.getUsername())
                .claim("accountType", account.getAccountType().name())
                .build();

        // 5. Ký token bằng HS256
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        // 6. Tạo refresh token và chỉ lưu bản băm trong database
        String refreshToken = refreshTokenService.create(account);

        // 7. Trả kết quả
        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }
}
