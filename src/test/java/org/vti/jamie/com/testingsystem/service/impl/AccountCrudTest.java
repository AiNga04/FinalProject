package org.vti.jamie.com.testingsystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vti.jamie.com.testingsystem.config.ModelMapperConfig;
import org.vti.jamie.com.testingsystem.dto.requests.CreateAccountRequest;
import org.vti.jamie.com.testingsystem.dto.requests.UpdateAccountRequest;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.exception.DuplicateResourceException;
import org.vti.jamie.com.testingsystem.exception.ResourceConflictException;
import org.vti.jamie.com.testingsystem.exception.ResourceNotFoundException;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccountCrudTest {
    private IAccountRepository repository;
    private IRefreshTokenRepository refreshTokens;
    private BCryptPasswordEncoder encoder;
    private AccountService service;

    @BeforeEach
    void setUp() {
        repository = mock(IAccountRepository.class);
        refreshTokens = mock(IRefreshTokenRepository.class);
        encoder = new BCryptPasswordEncoder();
        service = new AccountService(repository, new ModelMapperConfig().modelMapper(), encoder, refreshTokens);
    }

    @Test
    void createHashesPasswordAndSetsAuditAndActiveStatus() {
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var response = service.create(createRequest(), "3");
        var saved = ArgumentCaptor.forClass(Account.class);
        verify(repository).saveAndFlush(saved.capture());
        assertTrue(encoder.matches("test-password", saved.getValue().getPasswordHash()));
        assertEquals(EN_Status.ACTIVE, response.getStatus());
        assertEquals(3L, saved.getValue().getCreatedBy());
        assertEquals("new-user", response.getUsername());
    }

    @Test
    void createWithoutOptionalFieldsDefaultsToEmployeeAndNoDepartment() {
        var request = createRequest();
        request.setAccountType(null);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));

        var response = service.create(request, "3");

        assertEquals(EN_AccountType.EMPLOYEE, response.getAccountType());
        assertNull(response.getDepartmentId());
        verify(repository, never()).countActiveDepartment(any());
    }

    @Test
    void createKeepsExplicitAccountTypeAndDepartment() {
        var request = createRequest();
        request.setAccountType(EN_AccountType.MANAGER);
        request.setDepartmentId(9L);
        when(repository.countActiveDepartment(9L)).thenReturn(1L);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));

        var response = service.create(request, "3");

        assertEquals(EN_AccountType.MANAGER, response.getAccountType());
        assertEquals(9L, response.getDepartmentId());
    }

    @Test
    void cannotReuseUsernameOfSoftDeletedAccount() {
        when(repository.existsByUsername("new-user")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(createRequest(), "3"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateEmailIsRejected() {
        when(repository.existsByEmail("user@example.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(createRequest(), "3"));
    }

    @Test
    void missingDepartmentIsRejected() {
        var request = createRequest();
        request.setDepartmentId(9L);
        assertThrows(ResourceNotFoundException.class, () -> service.create(request, "3"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deleteOnlyChangesStatusAndRevokesSessions() {
        Account account = account(7L, EN_Status.ACTIVE);
        when(repository.findAllById(Set.of(7L))).thenReturn(List.of(account));
        service.updateStatusList(List.of(7L), EN_Status.DELETED, "3");
        assertEquals(EN_Status.DELETED, account.getStatus());
        assertEquals(3L, account.getUpdatedBy());
        assertEquals(1, account.getTokenVersion());
        verify(refreshTokens).revokeByAccountIds(eq(Set.of(7L)), any());
        verify(repository, never()).delete(any(Account.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void bulkDeleteRejectsMissingIdBeforeChangingAnyAccount() {
        Account account = account(7L, EN_Status.ACTIVE);
        when(repository.findAllById(Set.of(7L, 8L))).thenReturn(List.of(account));
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatusList(List.of(7L, 8L), EN_Status.DELETED, "3"));
        assertEquals(EN_Status.ACTIVE, account.getStatus());
        verify(repository, never()).saveAllAndFlush(any());
        verifyNoInteractions(refreshTokens);
    }

    @Test
    void bulkDeleteDeduplicatesIds() {
        Account account = account(7L, EN_Status.ACTIVE);
        when(repository.findAllById(Set.of(7L))).thenReturn(List.of(account));
        service.updateStatusList(List.of(7L, 7L), EN_Status.DELETED, "3");
        assertEquals(1, account.getTokenVersion());
    }

    @Test
    void adminCanRestoreDeletedAccountAndOldSessionsRemainInvalid() {
        Account account = account(7L, EN_Status.DELETED);
        account.setTokenVersion(1);
        when(repository.findAllById(Set.of(7L))).thenReturn(List.of(account));
        when(repository.saveAllAndFlush(any())).thenReturn(List.of(account));
        var response = service.updateStatusList(List.of(7L), EN_Status.ACTIVE, "3");
        assertEquals(EN_Status.ACTIVE, response.get(0).getStatus());
        assertEquals(2, account.getTokenVersion());
        verify(refreshTokens).revokeByAccountIds(eq(Set.of(7L)), any());
    }

    @Test
    void statusListAppliesEachSupportedStatusToAllAccounts() {
        for (EN_Status status : EN_Status.values()) {
            Account first = account(7L, EN_Status.DELETED);
            Account second = account(8L, EN_Status.ACTIVE);
            when(repository.findAllById(Set.of(7L, 8L))).thenReturn(List.of(first, second));
            when(repository.saveAllAndFlush(any())).thenReturn(List.of(first, second));

            var response = service.updateStatusList(List.of(7L, 8L), status, "3");

            assertEquals(2, response.size());
            assertEquals(status, response.get(0).getStatus());
            assertEquals(status, response.get(1).getStatus());
            assertEquals(3L, first.getUpdatedBy());
            assertEquals(3L, second.getUpdatedBy());
        }
    }

    @Test
    void deletedAccountMustBeRestoredBeforeEditingProfile() {
        when(repository.findById(7L)).thenReturn(Optional.of(account(7L, EN_Status.DELETED)));
        assertThrows(ResourceConflictException.class,
                () -> service.update(7L, updateRequest(), "3"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void roleChangeInvalidatesOldTokensAndKeepsPasswordAndStatus() {
        Account account = account(7L, EN_Status.ACTIVE);
        account.setPasswordHash("existing-password-hash");
        when(repository.findById(7L)).thenReturn(Optional.of(account));
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var request = updateRequest();
        request.setAccountType(EN_AccountType.MANAGER);
        var response = service.update(7L, request, "3");
        assertEquals(EN_AccountType.MANAGER, response.getAccountType());
        assertEquals(1, account.getTokenVersion());
        assertEquals("existing-password-hash", account.getPasswordHash());
        assertEquals(EN_Status.ACTIVE, account.getStatus());
        verify(refreshTokens).revokeByAccountIds(eq(List.of(7L)), any());
    }

    @Test
    void partialUpdateKeepsAllOmittedFieldsAndSessions() {
        Account account = account(7L, EN_Status.ACTIVE);
        account.setFirstName("Original");
        account.setLastName("Name");
        account.setDepartmentId(9L);
        account.setAccountType(EN_AccountType.MANAGER);
        account.setPasswordHash("existing-hash");
        when(repository.findById(7L)).thenReturn(Optional.of(account));
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFirstName(" Updated ");

        var response = service.update(7L, request, "3");

        assertEquals("Updated", response.getFirstName());
        assertEquals("Name", response.getLastName());
        assertEquals("new-user", response.getUsername());
        assertEquals("user@example.com", response.getEmail());
        assertEquals(EN_AccountType.MANAGER, response.getAccountType());
        assertEquals(9L, response.getDepartmentId());
        assertEquals("existing-hash", account.getPasswordHash());
        assertEquals(0, account.getTokenVersion());
        verifyNoInteractions(refreshTokens);
        verify(repository, never()).countActiveDepartment(any());
    }

    @Test
    void explicitNullDepartmentRemovesMembership() {
        Account account = account(7L, EN_Status.ACTIVE);
        account.setDepartmentId(9L);
        when(repository.findById(7L)).thenReturn(Optional.of(account));
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setDepartmentId(null);

        var response = service.update(7L, request, "3");

        assertNull(response.getDepartmentId());
        assertEquals("new-user", response.getUsername());
    }

    @Test
    void departmentOnlyUpdateValidatesAndChangesMembership() {
        Account account = account(7L, EN_Status.ACTIVE);
        when(repository.findById(7L)).thenReturn(Optional.of(account));
        when(repository.countActiveDepartment(9L)).thenReturn(1L);
        when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setDepartmentId(9L);

        assertEquals(9L, service.update(7L, request, "3").getDepartmentId());
        verify(repository).countActiveDepartment(9L);
    }

    @Test
    void singleStatusUpdateRestoresAccountAndRevokesOldSessions() {
        Account account = account(7L, EN_Status.DELETED);
        account.setTokenVersion(1);
        when(repository.findAllById(Set.of(7L))).thenReturn(List.of(account));
        when(repository.saveAllAndFlush(any())).thenReturn(List.of(account));

        var response = service.updateStatus(7L, EN_Status.ACTIVE, "3");

        assertEquals(7L, response.getId());
        assertEquals(EN_Status.ACTIVE, response.getStatus());
        assertEquals(2, account.getTokenVersion());
        assertEquals(3L, account.getUpdatedBy());
        verify(refreshTokens).revokeByAccountIds(eq(Set.of(7L)), any());
    }

    @Test
    void singleStatusUpdateRejectsUnknownId() {
        when(repository.findAllById(Set.of(99L))).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatus(99L, EN_Status.ACTIVE, "3"));
        verify(repository, never()).saveAllAndFlush(any());
        verifyNoInteractions(refreshTokens);
    }

    private Account account(Long id, EN_Status status) {
        Account account = new Account();
        account.setId(id);
        account.setUsername("new-user");
        account.setEmail("user@example.com");
        account.setStatus(status);
        account.setAccountType(EN_AccountType.EMPLOYEE);
        return account;
    }

    private CreateAccountRequest createRequest() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("new-user");
        request.setEmail("user@example.com");
        request.setPassword("test-password");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setAccountType(EN_AccountType.EMPLOYEE);
        return request;
    }

    private UpdateAccountRequest updateRequest() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setUsername("new-user");
        request.setEmail("user@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setAccountType(EN_AccountType.EMPLOYEE);
        return request;
    }
}
