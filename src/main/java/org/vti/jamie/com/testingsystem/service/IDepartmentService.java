package org.vti.jamie.com.testingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.testingsystem.form.department.CreateDepartmentForm;
import org.vti.jamie.com.testingsystem.form.department.DepartmentSearchForm;
import org.vti.jamie.com.testingsystem.form.department.UpdateDepartmentForm;
import org.vti.jamie.com.testingsystem.dto.department.DepartmentResponse;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.List;

public interface IDepartmentService {
    Page<DepartmentResponse> getDepartments(DepartmentSearchForm request, Pageable pageable);

    DepartmentResponse getDepartment(Long id);

    DepartmentResponse create(CreateDepartmentForm request, String subject);

    DepartmentResponse update(Long id, UpdateDepartmentForm request, String subject);

    DepartmentResponse updateStatus(Long id, EN_Status status, String subject);

    List<DepartmentResponse> updateStatusList(List<Long> ids, EN_Status status, String subject);
}
