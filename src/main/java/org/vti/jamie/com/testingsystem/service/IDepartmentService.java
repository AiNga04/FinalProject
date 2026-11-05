package org.vti.jamie.com.testingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.testingsystem.dto.requests.department.CreateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.DepartmentSearchRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.UpdateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.responses.department.DepartmentResponse;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.List;

public interface IDepartmentService {
    Page<DepartmentResponse> getDepartments(DepartmentSearchRequest request, Pageable pageable);

    DepartmentResponse getDepartment(Long id);

    DepartmentResponse create(CreateDepartmentRequest request, String subject);

    DepartmentResponse update(Long id, UpdateDepartmentRequest request, String subject);

    DepartmentResponse updateStatus(Long id, EN_Status status, String subject);

    List<DepartmentResponse> updateStatusList(List<Long> ids, EN_Status status, String subject);
}
