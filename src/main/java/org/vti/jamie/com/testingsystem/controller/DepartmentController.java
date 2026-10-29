package org.vti.jamie.com.testingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.vti.jamie.com.testingsystem.dto.requests.*;
import org.vti.jamie.com.testingsystem.dto.responses.ApiResponse;
import org.vti.jamie.com.testingsystem.dto.responses.DepartmentResponse;
import org.vti.jamie.com.testingsystem.service.IDepartmentService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {
    private final IDepartmentService departmentService;

    @GetMapping
    @Operation(summary = "ADMIN: danh sách phòng ban, tìm kiếm, lọc và phân trang")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> list(
            @Valid @ParameterObject @ModelAttribute DepartmentSearchRequest request) {
        Page<DepartmentResponse> page = departmentService.getDepartments(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách phòng ban thành công",
                page.getContent(), page.getTotalElements()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ADMIN: xem phòng ban, bao gồm phòng ban đã xóa")
    public ResponseEntity<ApiResponse<DepartmentResponse>> get(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy thông tin phòng ban thành công",
                departmentService.getDepartment(id)));
    }

    @PostMapping
    @Operation(summary = "ADMIN: tạo phòng ban, tùy chọn thêm tài khoản chưa thuộc phòng ban")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody CreateDepartmentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201,
                "Tạo phòng ban thành công", departmentService.create(request, jwt.getSubject())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "ADMIN: chỉ cập nhật các trường được truyền")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable @Positive Long id, @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật phòng ban thành công",
                departmentService.update(id, request, jwt.getSubject())));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "ADMIN: đổi trạng thái, DELETED để xóa mềm, ACTIVE/INACTIVE để khôi phục")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateStatus(
            @PathVariable @Positive Long id, @Valid @RequestBody UpdateDepartmentStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái phòng ban thành công",
                departmentService.updateStatus(id, request.status(), jwt.getSubject())));
    }

    @PatchMapping("/updateStatusList")
    @Operation(summary = "ADMIN: đổi trạng thái nhiều phòng ban, DELETED là xóa mềm")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> updateStatusList(
            @Valid @RequestBody UpdateDepartmentStatusListRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái các phòng ban thành công",
                departmentService.updateStatusList(request.ids(), request.status(), jwt.getSubject())));
    }
}
