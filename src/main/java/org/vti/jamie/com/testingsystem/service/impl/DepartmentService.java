package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vti.jamie.com.testingsystem.dto.requests.department.CreateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.DepartmentSearchRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.UpdateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.responses.department.DepartmentResponse;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.Department;
import org.vti.jamie.com.testingsystem.exception.DuplicateResourceException;
import org.vti.jamie.com.testingsystem.exception.ResourceConflictException;
import org.vti.jamie.com.testingsystem.exception.ResourceNotFoundException;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.repository.IDepartmentRepository;
import org.vti.jamie.com.testingsystem.service.IDepartmentService;
import org.vti.jamie.com.testingsystem.mapper.DepartmentMapper;
import org.vti.jamie.com.testingsystem.specification.DepartmentSpecification;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentService implements IDepartmentService {
    private final IDepartmentRepository departmentRepository;
    private final IAccountRepository accountRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getDepartments(DepartmentSearchRequest request, Pageable pageable) {
        if (request.getMinCreatedDate() != null && request.getMaxCreatedDate() != null
                && request.getMinCreatedDate().isAfter(request.getMaxCreatedDate())) {
            throw new IllegalArgumentException("Ngày tạo tối thiểu không được sau ngày tạo tối đa");
        }
        Specification<Department> filter = DepartmentSpecification.buildWhere(request);

        Pageable pagingOnly = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<Department> page = departmentRepository.findAll(filter, pagingOnly);
        Map<Long, Long> counts = memberCounts(page.getContent());
        return page.map(department -> departmentMapper.toResponse(department, counts.getOrDefault(department.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long id) {
        Department department = requireDepartment(id);
        return departmentMapper.toResponse(department, accountRepository.countByDepartmentIdAndStatusNot(id, EN_Status.DELETED));
    }

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request, String subject) {
        Department department = departmentMapper.toEntity(request);
        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateResourceException("Tên phòng ban đã tồn tại, kể cả trong phòng ban đã xóa");
        }
        List<Account> members = requireUnassignedAccounts(request.getAccountIds());
        department.setStatus(EN_Status.ACTIVE);
        department.setCreatedBy(Long.valueOf(subject));
        Department saved = departmentRepository.saveAndFlush(department);

        for (Account member : members) {
            member.setDepartmentId(saved.getId());
            member.setUpdatedBy(Long.valueOf(subject));
        }
        if (!members.isEmpty()) {
            accountRepository.saveAllAndFlush(members);
        }
        return departmentMapper.toResponse(saved, members.size());
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request, String subject) {
        Department department = requireDepartment(id);
        if (department.getStatus() == EN_Status.DELETED) {
            throw new ResourceConflictException("Hãy khôi phục phòng ban trước khi cập nhật thông tin");
        }
        if (request.getName() != null
                && departmentRepository.existsByNameAndIdNot(request.getName().strip(), id)) {
            throw new DuplicateResourceException("Tên phòng ban đã tồn tại, kể cả trong phòng ban đã xóa");
        }
        departmentMapper.updateEntity(request, department);
        department.setUpdatedBy(Long.valueOf(subject));
        Department saved = departmentRepository.saveAndFlush(department);
        return departmentMapper.toResponse(saved, accountRepository.countByDepartmentIdAndStatusNot(id, EN_Status.DELETED));
    }

    @Override
    @Transactional
    public DepartmentResponse updateStatus(Long id, EN_Status status, String subject) {
        return updateStatusList(List.of(id), status, subject).get(0);
    }

    @Override
    @Transactional
    public List<DepartmentResponse> updateStatusList(List<Long> ids, EN_Status status, String subject) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        List<Department> departments = departmentRepository.findAllById(uniqueIds);
        if (departments.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều phòng ban không tồn tại");
        }
        if (status == EN_Status.DELETED
                && departments.stream().anyMatch(department -> department.getStatus() == EN_Status.DELETED)) {
            throw new ResourceConflictException("Một hoặc nhiều phòng ban đã bị xóa");
        }
        for (Department department : departments) {
            department.setStatus(status);
            department.setUpdatedBy(Long.valueOf(subject));
        }
        // Soft delete không xóa tài khoản hay liên kết phòng ban.
        List<Department> saved = departmentRepository.saveAllAndFlush(departments);
        Map<Long, Long> counts = memberCounts(saved);
        return saved.stream().map(department ->
                departmentMapper.toResponse(department, counts.getOrDefault(department.getId(), 0L))).toList();
    }

    private Department requireDepartment(Long id) {
        return departmentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Không tìm thấy phòng ban"));
    }

    private List<Account> requireUnassignedAccounts(List<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>(accountIds);
        List<Account> accounts = accountRepository.findAllById(ids);
        if (accounts.size() != ids.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều tài khoản không tồn tại");
        }
        for (Account account : accounts) {
            if (account.getStatus() == EN_Status.DELETED) {
                throw new ResourceConflictException("Không thể thêm tài khoản đã xóa vào phòng ban");
            }
            if (account.getDepartmentId() != null) {
                throw new ResourceConflictException("Tài khoản đã thuộc phòng ban khác");
            }
        }
        return accounts;
    }

    private Map<Long, Long> memberCounts(List<Department> departments) {
        Map<Long, Long> counts = new HashMap<>();
        if (!departments.isEmpty()) {
            List<Long> ids = departments.stream().map(Department::getId).toList();
            for (var count : accountRepository.countMembersByDepartmentIds(ids, EN_Status.DELETED)) {
                counts.put(count.getDepartmentId(), count.getTotalMember());
            }
        }
        return counts;
    }

}
