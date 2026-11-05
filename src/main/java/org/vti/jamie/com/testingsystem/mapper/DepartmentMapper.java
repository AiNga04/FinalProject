package org.vti.jamie.com.testingsystem.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.testingsystem.dto.requests.department.CreateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.requests.department.UpdateDepartmentRequest;
import org.vti.jamie.com.testingsystem.dto.responses.department.DepartmentResponse;
import org.vti.jamie.com.testingsystem.entity.Department;

@Component
@RequiredArgsConstructor
public class DepartmentMapper {

    private final ModelMapper modelMapper;

    public Department toEntity(CreateDepartmentRequest request) {
        Department department = modelMapper.map(request, Department.class);
        department.setName(request.getName().strip());
        department.setType(request.getType().strip());
        return department;
    }

    public void updateEntity(
            UpdateDepartmentRequest request,
            Department department) {

        modelMapper.map(request, department);
        if (request.getName() != null) {
            department.setName(request.getName().strip());
        }
        if (request.getType() != null) {
            department.setType(request.getType().strip());
        }
    }

    public DepartmentResponse toResponse(
            Department department,
            long totalMember) {

        DepartmentResponse response =
                modelMapper.map(department, DepartmentResponse.class);

        response.setTotalMember(totalMember);
        return response;
    }
}
