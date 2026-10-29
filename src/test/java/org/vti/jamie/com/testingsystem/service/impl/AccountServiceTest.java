package org.vti.jamie.com.testingsystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.vti.jamie.com.testingsystem.config.ModelMapperConfig;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    private IAccountRepository repository;
    private AccountService service;

    @BeforeEach
    void setUp() {
        repository = mock(IAccountRepository.class);
        service = new AccountService(repository, new ModelMapperConfig().modelMapper(),
                new BCryptPasswordEncoder(), mock(IRefreshTokenRepository.class));
    }

    @Test
    void readsCurrentDatabaseProfileWithoutExposingPasswordHash() {
        Account account = new Account();
        account.setId(3L);
        account.setUsername("current-name");
        account.setEmail("user@example.invalid");
        account.setAccountType(EN_AccountType.MANAGER);
        account.setDepartmentId(7L);
        account.setPasswordHash("must-not-be-returned");
        when(repository.findById(3L)).thenReturn(Optional.of(account));

        var response = service.getMe("3");

        assertEquals(3L, response.getId());
        assertEquals("current-name", response.getUsername());
        assertEquals("user@example.invalid", response.getEmail());
        assertEquals(EN_AccountType.MANAGER, response.getAccountType());
        assertEquals(7L, response.getDepartmentId());
        var json = new JsonMapper().writeValueAsString(response);
        assertFalse(json.contains("password"));
        assertFalse(json.contains("must-not-be-returned"));
    }

    @Test
    void missingAccountReturnsUnauthorized() {
        when(repository.findById(3L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class, () -> service.getMe("3"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void inactiveAndDeletedAccountsAreRejected() {
        for (var status : new EN_Status[]{EN_Status.INACTIVE, EN_Status.DELETED}) {
            Account account = new Account();
            account.setStatus(status);
            when(repository.findById(3L)).thenReturn(Optional.of(account));
            var exception = assertThrows(ResponseStatusException.class,
                    () -> service.getMe("3"));
            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        }
    }

    @Test
    void malformedSubjectsAreRejectedBeforeQueryingDatabase() {
        for (String subject : new String[]{null, "abc", "0", "-1"}) {
            var exception = assertThrows(ResponseStatusException.class,
                    () -> service.getMe(subject));
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }
        verifyNoInteractions(repository);
    }
}
