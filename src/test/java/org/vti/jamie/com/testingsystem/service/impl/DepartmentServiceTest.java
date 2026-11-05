package org.vti.jamie.com.testingsystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.vti.jamie.com.testingsystem.config.ModelMapperConfig;
import org.vti.jamie.com.testingsystem.mapper.DepartmentMapper;
import org.vti.jamie.com.testingsystem.dto.requests.department.CreateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.DepartmentSearchRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.UpdateDepartmentRequest;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.Department;
import org.vti.jamie.com.testingsystem.exception.*;
import org.vti.jamie.com.testingsystem.repository.*;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {
    private IDepartmentRepository departments;
    private IAccountRepository accounts;
    private DepartmentService service;

    @BeforeEach
    void setUp() {
        departments = mock(IDepartmentRepository.class);
        accounts = mock(IAccountRepository.class);
        service = new DepartmentService(departments, accounts, new DepartmentMapper(new ModelMapperConfig().modelMapper()));
    }

    @Test
    void createWithoutMembersSetsActiveStatusAndAudit() {
        stubSave();
        var response = service.create(createRequest(), "3");
        assertEquals(10L, response.getId());
        assertEquals("Engineering", response.getName());
        assertEquals("DEV", response.getType());
        assertEquals(EN_Status.ACTIVE, response.getStatus());
        assertEquals(3L, response.getCreatedBy());
        assertEquals(0, response.getTotalMember());
        verifyNoInteractions(accounts);
    }

    @Test
    void createAssignsUnassignedAccountsAndDeduplicatesIds() {
        stubSave();
        var request = createRequest();
        request.setAccountIds(List.of(7L, 7L, 8L));
        Account first = new Account();
        Account second = new Account();
        when(accounts.findAllById(Set.of(7L, 8L))).thenReturn(List.of(first, second));

        var response = service.create(request, "3");

        assertEquals(2, response.getTotalMember());
        assertEquals(10L, first.getDepartmentId());
        assertEquals(10L, second.getDepartmentId());
        assertEquals(3L, first.getUpdatedBy());
        verify(accounts).saveAllAndFlush(List.of(first, second));
    }

    @Test
    void duplicateNameIncludingDeletedDepartmentIsRejected() {
        when(departments.existsByName("Engineering")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(createRequest(), "3"));
        verify(departments, never()).saveAndFlush(any());
    }

    @Test
    void missingAccountIsRejectedBeforeCreatingDepartment() {
        var request = createRequest();
        request.setAccountIds(List.of(7L));
        when(accounts.findAllById(Set.of(7L))).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request, "3"));
        verify(departments, never()).saveAndFlush(any());
    }

    @Test
    void cannotAssignDeletedOrAlreadyAssignedAccount() {
        var request = createRequest();
        request.setAccountIds(List.of(7L));
        Account account = new Account();
        when(accounts.findAllById(Set.of(7L))).thenReturn(List.of(account));
        account.setStatus(EN_Status.DELETED);
        assertThrows(ResourceConflictException.class, () -> service.create(request, "3"));
        account.setStatus(EN_Status.ACTIVE);
        account.setDepartmentId(5L);
        assertThrows(ResourceConflictException.class, () -> service.create(request, "3"));
        assertEquals(5L, account.getDepartmentId());
        verify(departments, never()).saveAndFlush(any());
        verify(accounts, never()).saveAllAndFlush(any());
    }

    @Test
    void partialUpdatePreservesOmittedTypeAndCreationAudit() {
        Department department = department(10L, EN_Status.ACTIVE);
        department.setCreatedBy(1L);
        when(departments.findById(10L)).thenReturn(Optional.of(department));
        when(departments.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName(" New name ");

        var response = service.update(10L, request, "3");

        assertEquals("New name", response.getName());
        assertEquals("DEV", response.getType());
        assertEquals(EN_Status.ACTIVE, response.getStatus());
        assertEquals(1L, response.getCreatedBy());
        assertEquals(3L, response.getUpdatedBy());
    }

    @Test
    void typeOnlyUpdateNormalizesValueAndKeepsDepartmentName() {
        Department department = department(10L, EN_Status.ACTIVE);
        when(departments.findById(10L)).thenReturn(Optional.of(department));
        when(departments.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setType(" HR ");

        var response = service.update(10L, request, "3");

        assertEquals("Engineering", response.getName());
        assertEquals("HR", response.getType());
        verify(departments, never()).existsByNameAndIdNot(anyString(), anyLong());
    }

    @Test
    void deletedDepartmentMustBeRestoredBeforeEditing() {
        when(departments.findById(10L)).thenReturn(Optional.of(department(10L, EN_Status.DELETED)));
        assertThrows(ResourceConflictException.class,
                () -> service.update(10L, new UpdateDepartmentRequest(), "3"));
        verify(departments, never()).saveAndFlush(any());
    }

    @Test
    void duplicateNameOnUpdateIsRejected() {
        when(departments.findById(10L)).thenReturn(Optional.of(department(10L, EN_Status.ACTIVE)));
        when(departments.existsByNameAndIdNot("Existing", 10L)).thenReturn(true);
        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Existing");
        assertThrows(DuplicateResourceException.class, () -> service.update(10L, request, "3"));
    }

    @Test
    void missingDepartmentReturnsNotFound() {
        when(departments.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getDepartment(99L));
    }

    @Test
    void softDeleteAndRestoreDoNotChangeAccountMembershipOrStatus() {
        Department department = department(10L, EN_Status.ACTIVE);
        when(departments.findAllById(Set.of(10L))).thenReturn(List.of(department));
        when(departments.saveAllAndFlush(any())).thenReturn(List.of(department));
        assertEquals(EN_Status.DELETED, service.updateStatus(10L, EN_Status.DELETED, "3").getStatus());
        assertEquals(EN_Status.ACTIVE, service.updateStatus(10L, EN_Status.ACTIVE, "3").getStatus());
        assertEquals(3L, department.getUpdatedBy());
        verify(departments, never()).deleteById(anyLong());
        verify(accounts, never()).saveAllAndFlush(any());
    }

    @Test
    void deletingSameDepartmentTwiceRejectsSecondRequestWithoutChangingAudit() {
        Department department = department(10L, EN_Status.ACTIVE);
        when(departments.findAllById(Set.of(10L))).thenReturn(List.of(department));
        when(departments.saveAllAndFlush(any())).thenReturn(List.of(department));

        service.updateStatus(10L, EN_Status.DELETED, "3");
        assertThrows(ResourceConflictException.class,
                () -> service.updateStatus(10L, EN_Status.DELETED, "4"));

        assertEquals(3L, department.getUpdatedBy());
        verify(departments, times(1)).saveAllAndFlush(any());
    }

    @Test
    void bulkDeleteRejectsAlreadyDeletedDepartmentBeforeChangingAnyDepartment() {
        Department active = department(10L, EN_Status.ACTIVE);
        Department deleted = department(11L, EN_Status.DELETED);
        when(departments.findAllById(Set.of(10L, 11L))).thenReturn(List.of(active, deleted));

        assertThrows(ResourceConflictException.class,
                () -> service.updateStatusList(List.of(10L, 11L), EN_Status.DELETED, "3"));

        assertEquals(EN_Status.ACTIVE, active.getStatus());
        assertNull(active.getUpdatedBy());
        verify(departments, never()).saveAllAndFlush(any());
        verifyNoInteractions(accounts);
    }

    @Test
    void batchRejectsMissingIdBeforeChangingAnyDepartment() {
        Department department = department(10L, EN_Status.ACTIVE);
        when(departments.findAllById(Set.of(10L, 11L))).thenReturn(List.of(department));
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatusList(List.of(10L, 11L), EN_Status.DELETED, "3"));
        assertEquals(EN_Status.ACTIVE, department.getStatus());
        verify(departments, never()).saveAllAndFlush(any());
    }

    @Test
    void listLoadsMemberCountsInOneQueryAndPreservesPaginationTotal() {
        Department first = department(10L, EN_Status.ACTIVE);
        Department second = department(11L, EN_Status.INACTIVE);
        when(departments.findAll(org.mockito.ArgumentMatchers.<Specification<Department>>any(),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(first, second),
                PageRequest.of(0, 10), 16));
        DepartmentMemberCount count = new DepartmentMemberCount() {
            public Long getDepartmentId() { return 10L; }
            public long getTotalMember() { return 4; }
        };
        when(accounts.countMembersByDepartmentIds(List.of(10L, 11L), EN_Status.DELETED))
                .thenReturn(List.of(count));

        var page = service.getDepartments(new DepartmentSearchRequest(), PageRequest.of(0, 10));

        assertEquals(16, page.getTotalElements());
        assertEquals(4, page.getContent().get(0).getTotalMember());
        assertEquals(0, page.getContent().get(1).getTotalMember());
        verify(accounts).countMembersByDepartmentIds(List.of(10L, 11L), EN_Status.DELETED);
        verify(accounts, never()).countByDepartmentIdAndStatusNot(any(), any());
    }

    @Test
    void invalidDateRangeIsRejectedBeforeDatabaseQuery() {
        DepartmentSearchRequest request = new DepartmentSearchRequest();
        request.setMinCreatedDate(LocalDate.of(2026, 10, 29));
        request.setMaxCreatedDate(LocalDate.of(2026, 10, 1));
        assertThrows(IllegalArgumentException.class,
                () -> service.getDepartments(request, PageRequest.of(0, 10)));
        verifyNoInteractions(departments, accounts);
    }

    private void stubSave() {
        when(departments.saveAndFlush(any())).thenAnswer(call -> {
            Department department = call.getArgument(0);
            department.setId(10L);
            return department;
        });
    }

    private Department department(Long id, EN_Status status) {
        Department department = new Department();
        department.setId(id);
        department.setName("Engineering");
        department.setType("DEV");
        department.setStatus(status);
        return department;
    }

    private CreateDepartmentRequest createRequest() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName(" Engineering ");
        request.setType(" DEV ");
        return request;
    }
}
