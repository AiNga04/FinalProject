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
import org.vti.jamie.com.testingsystem.dto.PageResponse;
import org.vti.jamie.com.testingsystem.service.IAccountService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.vti.jamie.com.testingsystem.dto.account.AvatarResponse;
import org.vti.jamie.com.testingsystem.service.AccountAvatarService;
import java.nio.file.Files;

@RestController
@Validated
@RequestMapping("/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final IAccountService accountService;
    private final AccountAvatarService accountAvatarService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    public ResponseEntity<ApiResponse<AccountResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        AccountResponse account = accountService.getMe(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                200, "Lấy thông tin tài khoản thành công", account
        ));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật avatar tài khoản hiện tại")
    public ResponseEntity<ApiResponse<AvatarResponse>> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        Long accountId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật avatar thành công",
                accountAvatarService.upload(accountId, file, accountId)));
    }

    @GetMapping("/me/avatar")
    @Operation(summary = "Lấy thông tin avatar hiện tại")
    public ResponseEntity<ApiResponse<AvatarResponse>> currentAvatar(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy avatar thành công",
                accountAvatarService.getCurrent(Long.valueOf(jwt.getSubject()))));
    }

    @GetMapping("/{accountId}/avatar")
    @Operation(summary = "ADMIN: lấy thông tin avatar của tài khoản")
    public ResponseEntity<ApiResponse<AvatarResponse>> accountAvatar(
            @PathVariable @Positive Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy avatar tài khoản thành công",
                accountAvatarService.getCurrent(accountId)));
    }

    @GetMapping("/me/avatar/file")
    @Operation(summary = "Lấy file avatar hiện tại của chính mình")
    public ResponseEntity<Resource> currentAvatarFile(
            @AuthenticationPrincipal Jwt jwt) throws java.io.IOException {
        var path = accountAvatarService.getCurrentFilePath(Long.valueOf(jwt.getSubject()));
        Resource resource = new FileSystemResource(path);
        String detectedType = Files.probeContentType(path);
        MediaType contentType = detectedType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(detectedType);
        return ResponseEntity.ok().contentType(contentType).body(resource);
    }

    @GetMapping("/me/avatar/{avatarId}/file")
    @Operation(summary = "Lấy file avatar trong lịch sử của chính mình")
    public ResponseEntity<Resource> myAvatarFile(
            @PathVariable @Positive Long avatarId,
            @AuthenticationPrincipal Jwt jwt) throws java.io.IOException {
        var path = accountAvatarService.getFilePath(avatarId, Long.valueOf(jwt.getSubject()));
        Resource resource = new FileSystemResource(path);
        String detectedType = Files.probeContentType(path);
        MediaType contentType = detectedType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(detectedType);
        return ResponseEntity.ok().contentType(contentType).body(resource);
    }

    @GetMapping("/me/avatar/history")
    @Operation(summary = "Lấy lịch sử avatar tài khoản hiện tại")
    public ResponseEntity<ApiResponse<List<AvatarResponse>>> avatarHistory(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy lịch sử avatar thành công",
                accountAvatarService.getHistory(Long.valueOf(jwt.getSubject()))));
    }

    @GetMapping("/{accountId}/avatars/history")
    @Operation(summary = "ADMIN: xem lịch sử avatar của tài khoản")
    public ResponseEntity<ApiResponse<List<AvatarResponse>>> accountAvatarHistory(
            @PathVariable @Positive Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy lịch sử avatar thành công",
                accountAvatarService.getHistory(accountId)));
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Xóa mềm avatar hiện tại")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar(
            @AuthenticationPrincipal Jwt jwt) {
        Long accountId = Long.valueOf(jwt.getSubject());
        accountAvatarService.deleteCurrent(accountId, accountId);
        return ResponseEntity.ok(ApiResponse.success("Xóa avatar thành công", null));
    }

    @DeleteMapping("/me/avatar/{avatarId}")
    @Operation(summary = "Xóa avatar trong lịch sử của chính mình")
    public ResponseEntity<ApiResponse<Void>> deleteMyAvatar(
            @PathVariable @Positive Long avatarId,
            @AuthenticationPrincipal Jwt jwt) {
        accountAvatarService.delete(avatarId, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Xóa avatar thành công", null));
    }

    @DeleteMapping("/{accountId}/avatars/{avatarId}")
    @Operation(summary = "ADMIN: xóa mềm một avatar trong lịch sử")
    public ResponseEntity<ApiResponse<Void>> deleteAccountAvatar(
            @PathVariable @Positive Long accountId,
            @PathVariable @Positive Long avatarId,
            @AuthenticationPrincipal Jwt jwt) {
        accountAvatarService.delete(avatarId, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("Xóa avatar thành công", null));
    }

    @GetMapping("/avatars/{avatarId}/file")
    @Operation(summary = "Lấy file avatar")
    public ResponseEntity<Resource> avatarFile(@PathVariable @Positive Long avatarId) throws java.io.IOException {
        var path = accountAvatarService.getFilePath(avatarId);
        Resource resource = new FileSystemResource(path);
        String detectedType = Files.probeContentType(path);
        MediaType contentType = detectedType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(detectedType);
        return ResponseEntity.ok().contentType(contentType).body(resource);
    }

    @PatchMapping("/{accountId}/avatars/{avatarId}/restore")
    @Operation(summary = "Khôi phục avatar")
    public ResponseEntity<ApiResponse<AvatarResponse>> restoreAvatar(
            @PathVariable @Positive Long accountId,
            @PathVariable @Positive Long avatarId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                "Khôi phục avatar thành công",
                accountAvatarService.restore(avatarId, Long.valueOf(jwt.getSubject()))));
    }

    @PatchMapping("/me/avatar/{avatarId}/restore")
    @Operation(summary = "Chọn lại avatar trong lịch sử làm avatar hiện tại")
    public ResponseEntity<ApiResponse<AvatarResponse>> restoreMyAvatar(
            @PathVariable @Positive Long avatarId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt avatar thành công",
                accountAvatarService.restore(avatarId, Long.valueOf(jwt.getSubject()))));
    }

    @GetMapping
    @Operation(summary = "ADMIN: danh sách tài khoản, tìm kiếm, lọc và phân trang")
    public ResponseEntity<ApiResponse<PageResponse<AccountResponse>>> list(
            @Valid @ParameterObject
            @ModelAttribute AccountSearchForm request,
            @ParameterObject Pageable pageable) {
        Page<AccountResponse> page = accountService.getAccounts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tài khoản thành công",
                PageResponse.from(page))
        );
    }

    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "ADMIN: xuất danh sách tài khoản ra Excel")
    public ResponseEntity<byte[]> export(
            @Valid @ParameterObject @ModelAttribute AccountSearchForm request) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=accounts.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(accountService.exportAccounts(request));
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
