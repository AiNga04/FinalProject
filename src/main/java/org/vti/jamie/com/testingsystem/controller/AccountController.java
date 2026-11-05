package org.vti.jamie.com.testingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.vti.jamie.com.testingsystem.form.account.*;
import org.vti.jamie.com.testingsystem.dto.account.AccountResponse;
import org.vti.jamie.com.testingsystem.dto.ApiResponse;
import org.vti.jamie.com.testingsystem.service.IAccountService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final IAccountService accountService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    public ResponseEntity<ApiResponse<AccountResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        AccountResponse account = accountService.getMe(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                200, "Lấy thông tin tài khoản thành công", account
        ));
    }

    @GetMapping
    @Operation(summary = "ADMIN: danh sách tài khoản, tìm kiếm, lọc và phân trang")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(
            @Valid @ParameterObject
            @ModelAttribute AccountSearchForm request,
            @ParameterObject Pageable pageable) {
        Page<AccountResponse> page = accountService.getAccounts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Lấy danh sách tài khoản thành công",
                page.getContent(),
                page.getTotalElements())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "ADMIN: xem tài khoản, bao gồm tài khoản đã xóa")
    public ResponseEntity<ApiResponse<AccountResponse>> get(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Lấy thông tin tài khoản thành công",
                accountService.getAccount(id))
        );
    }

    @PostMapping
    @Operation(summary = "ADMIN: tạo tài khoản")
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @Valid @RequestBody CreateAccountForm request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                201,
                "Tạo tài khoản thành công",
                accountService.create(request, jwt.getSubject()))
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "ADMIN: cập nhật các trường được truyền; departmentId=null để bỏ phòng ban")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateAccountForm request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Cập nhật tài khoản thành công",
                accountService.update(id, request, jwt.getSubject()))
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "ADMIN: đổi trạng thái một tài khoản, hỗ trợ xóa mềm và khôi phục")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateAccountStatusForm request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Cập nhật trạng thái thành công",
                accountService.updateStatus(id, request.status(), jwt.getSubject()))
        );
    }

    @PatchMapping("/updateStatusList")
    @Operation(summary = "ADMIN: đổi trạng thái hàng loạt, DELETED để xóa mềm, ACTIVE/INACTIVE để khôi phục")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> updateStatusList(
            @Valid @RequestBody UpdateAccountStatusListForm request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Cập nhật trạng thái thành công",
                accountService.updateStatusList(request.ids(),
                        request.status(), jwt.getSubject()))
        );
    }
}
