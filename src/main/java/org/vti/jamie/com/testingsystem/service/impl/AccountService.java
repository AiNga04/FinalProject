package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vti.jamie.com.testingsystem.form.account.AccountSearchForm;
import org.vti.jamie.com.testingsystem.form.account.CreateAccountForm;
import org.vti.jamie.com.testingsystem.form.account.UpdateAccountForm;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.exception.DuplicateResourceException;
import org.vti.jamie.com.testingsystem.exception.ResourceConflictException;
import org.vti.jamie.com.testingsystem.exception.ResourceNotFoundException;
import org.vti.jamie.com.testingsystem.mapper.AccountMapper;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.vti.jamie.com.testingsystem.dto.account.AccountResponse;
import org.vti.jamie.com.testingsystem.dto.account.AccountExportRow;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.service.IAccountService;
import org.vti.jamie.com.testingsystem.specification.AccountSpecification;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {
    private final IAccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMe(String subject) {
        long accountId;
        try {
            accountId = Long.parseLong(subject);
            if (accountId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Access token không hợp lệ");
        }

        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Tài khoản không còn tồn tại"));

        if (account.getStatus() != EN_Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tài khoản chưa được kích hoạt hoặc đã bị khóa");
        }

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(
            AccountSearchForm request,
            Pageable pageable) {

        Specification<Account> where =
                AccountSpecification.buildWhere(request);

        Pageable pagingOnly = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return accountRepository.findAll(where, pagingOnly)
                .map(accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAccounts(AccountSearchForm request) {
        List<AccountExportRow> accounts = accountRepository
                .findAll(AccountSpecification.buildWhere(request))
                .stream()
                .map(accountMapper::toExportRow)
                .toList();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Accounts");
            String[] headers = {"ID", "Username", "Email", "Họ", "Tên", "Vai trò", "Department ID", "Trạng thái", "Ngày tạo"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            Row header = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(headers[index]);
                cell.setCellStyle(headerStyle);
            }
            for (int rowIndex = 0; rowIndex < accounts.size(); rowIndex++) {
                AccountExportRow account = accounts.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);
                row.createCell(0).setCellValue(account.getId());
                row.createCell(1).setCellValue(account.getUsername());
                row.createCell(2).setCellValue(account.getEmail());
                row.createCell(3).setCellValue(account.getLastName());
                row.createCell(4).setCellValue(account.getFirstName());
                row.createCell(5).setCellValue(account.getAccountType());
                if (account.getDepartmentId() != null) row.createCell(6).setCellValue(account.getDepartmentId());
                row.createCell(7).setCellValue(account.getStatus());
                row.createCell(8).setCellValue(account.getCreatedAt().toString());
            }
            for (int index = 0; index < headers.length; index++) sheet.autoSizeColumn(index);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể tạo file Excel tài khoản", exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        // ADMIN có thể xem cả bản ghi đã soft delete để kiểm tra/khôi phục.
        return accountMapper.toResponse(requireAccount(id));
    }

    @Override
    @Transactional
    public AccountResponse create(CreateAccountForm request, String subject) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password không được vượt quá 72 byte UTF-8");
        }
        Account account = accountMapper.toEntity(request);
        checkUnique(account.getUsername(), account.getEmail(), null);
        checkDepartment(account.getDepartmentId());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus(EN_Status.ACTIVE);
        account.setCreatedBy(Long.valueOf(subject));
        return accountMapper.toResponse(accountRepository.saveAndFlush(account));
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, UpdateAccountForm request, String subject) {
        Account account = requireAccount(id);
        if (account.getStatus() == EN_Status.DELETED) {
            throw new ResourceConflictException("Hãy khôi phục tài khoản trước khi cập nhật thông tin");
        }
        String username = request.getUsername() == null
                ? account.getUsername() : request.getUsername().strip();
        String email = request.getEmail() == null
                ? account.getEmail() : request.getEmail().strip();
        if (request.getUsername() != null || request.getEmail() != null) {
            checkUnique(username, email, id);
        }
        if (request.isDepartmentIdPresent()) {
            checkDepartment(request.getDepartmentId());
        }
        boolean identityChanged = !Objects.equals(account.getUsername(), username)
                || (request.getAccountType() != null
                    && account.getAccountType() != request.getAccountType());
        accountMapper.updateEntity(request, account);
        account.setUpdatedBy(Long.valueOf(subject));
        if (identityChanged) {
            account.setTokenVersion(account.getTokenVersion() + 1);
            refreshTokenRepository.revokeByAccountIds(List.of(id), Instant.now());
        }
        return accountMapper.toResponse(accountRepository.saveAndFlush(account));
    }

    @Override
    @Transactional
    public List<AccountResponse> updateStatusList(List<Long> ids, EN_Status status, String subject) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        List<Account> accounts = accountRepository.findAllById(uniqueIds);
        if (accounts.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều tài khoản không tồn tại");
        }
        if (status == EN_Status.DELETED
                && accounts.stream().anyMatch(account -> account.getStatus() == EN_Status.DELETED)) {
            throw new ResourceConflictException("Một hoặc nhiều tài khoản đã bị xóa");
        }
        for (Account account : accounts) {
            // DELETED là soft delete; ACTIVE/INACTIVE cũng áp dụng cho bản ghi đã xóa.
            account.setStatus(status);
            account.setTokenVersion(account.getTokenVersion() + 1);
            account.setUpdatedBy(Long.valueOf(subject));
        }
        List<Account> updated = accountRepository.saveAllAndFlush(accounts);
        refreshTokenRepository.revokeByAccountIds(uniqueIds, Instant.now());
        return updated.stream().map(accountMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AccountResponse updateStatus(Long id, EN_Status status, String subject) {
        return updateStatusList(List.of(id), status, subject).get(0);
    }

    private Account requireAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    private void checkUnique(String username, String email, Long id) {
        boolean duplicateUsername = id == null ? accountRepository.existsByUsername(username)
                : accountRepository.existsByUsernameAndIdNot(username, id);
        if (duplicateUsername) {
            throw new DuplicateResourceException("Username đã tồn tại, kể cả trong tài khoản đã xóa");
        }
        boolean duplicateEmail = id == null ? accountRepository.existsByEmail(email)
                : accountRepository.existsByEmailAndIdNot(email, id);
        if (duplicateEmail) {
            throw new DuplicateResourceException("Email đã tồn tại, kể cả trong tài khoản đã xóa");
        }
    }

    private void checkDepartment(Long departmentId) {
        if (departmentId != null && accountRepository.countActiveDepartment(departmentId) == 0) {
            throw new ResourceNotFoundException("Phòng ban không tồn tại hoặc không hoạt động");
        }
    }
}
