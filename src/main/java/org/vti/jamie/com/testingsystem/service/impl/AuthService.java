package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.vti.jamie.com.testingsystem.dto.responses.AccountResponse;
import org.vti.jamie.com.testingsystem.dto.responses.LoginResponse;
import org.vti.jamie.com.testingsystem.repository.AccountRepository;
import org.vti.jamie.com.testingsystem.service.IAuthService;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-minutes}")
    private long accessTokenMinutes;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. Authenticate username/password
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Username hoặc password không chính xác"
            );
        }

        // 2. Load verified account
        var account = accountRepository
            .findByUsername(loginRequest.username())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
            ));

        // 3. Verify account status
        if (account.getStatus() != EN_Status.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Tài khoản chưa được kích hoạt hoặc đã bị khóa"
            );
        }

        // 4. Create JWT claims
        Instant now = Instant.now();

        Instant expiry = now.plus(
                accessTokenMinutes, ChronoUnit.MINUTES
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(account.getId().toString())
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("username", account.getUsername())
                .claim(
                        "accountType",
                        account.getAccountType().name()
                )
                .build();

        // 5. Sign JWT
        JwsHeader header =
                JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        // 6. Return response
        return new LoginResponse(
                token,
                "Bearer",
                accessTokenMinutes * 60
        );
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(
            int page,
            int size) {

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        return accountRepository
                .findByStatusNot(EN_Status.DELETED, pageable)
                .map(AccountResponse::from);
    }
}
