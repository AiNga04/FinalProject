package org.vti.jamie.com.testingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vti.jamie.com.testingsystem.dto.requests.*;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.exception.DuplicateResourceException;
import org.vti.jamie.com.testingsystem.exception.ResourceConflictException;
import org.vti.jamie.com.testingsystem.exception.ResourceNotFoundException;
import org.vti.jamie.com.testingsystem.repository.IRefreshTokenRepository;
import org.vti.jamie.com.testingsystem.dto.responses.AccountResponse;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.service.IAccountService;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {
    private final IAccountRepository accountRepository;
    private final ModelMapper modelMapper;
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

        return modelMapper.map(account, AccountResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(AccountSearchRequest request) {
        Specification<Account> filter = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            // Chỉ trả DELETED khi ADMIN chủ động lọc status=DELETED.
            predicates.add(request.getStatus() == null
                    ? cb.notEqual(root.get("status"), EN_Status.DELETED)
                    : cb.equal(root.get("status"), request.getStatus()));
            if (request.getAccountType() != null) {
                predicates.add(cb.equal(root.get("accountType"), request.getAccountType()));
            }
            if (request.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), request.getDepartmentId()));
            }
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String value = request.getSearch().strip().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                String pattern = "%" + value + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern, '\\'),
                        cb.like(cb.lower(root.get("firstName")), pattern, '\\'),
                        cb.like(cb.lower(root.get("lastName")), pattern, '\\')));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return accountRepository.findAll(filter,
                        PageRequest.of(request.getPage(), request.getSize()))
                .map(account -> modelMapper.map(account, AccountResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        // ADMIN có thể xem cả bản ghi đã soft delete để kiểm tra/khôi phục.
        return modelMapper.map(requireAccount(id), AccountResponse.class);
    }

    @Override
    @Transactional
    public AccountResponse create(CreateAccountRequest request, String subject) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password không được vượt quá 72 byte UTF-8");
        }
        String username = request.getUsername().strip();
        String email = request.getEmail().strip();
        checkUnique(username, email, null);
        checkDepartment(request.getDepartmentId());
        Account account = modelMapper.map(request, Account.class);
        account.setAccountType(request.getAccountType() == null
                ? EN_AccountType.EMPLOYEE : request.getAccountType());
        account.setUsername(username);
        account.setEmail(email);
        account.setFirstName(request.getFirstName().strip());
        account.setLastName(request.getLastName().strip());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus(EN_Status.ACTIVE);
        account.setCreatedBy(Long.valueOf(subject));
        return modelMapper.map(accountRepository.saveAndFlush(account), AccountResponse.class);
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, UpdateAccountRequest request, String subject) {
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
        modelMapper.map(request, account);
        account.setUsername(username);
        account.setEmail(email);
        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName().strip());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName().strip());
        }
        if (request.isDepartmentIdPresent()) {
            account.setDepartmentId(request.getDepartmentId());
        }
        account.setUpdatedBy(Long.valueOf(subject));
        if (identityChanged) {
            account.setTokenVersion(account.getTokenVersion() + 1);
            refreshTokenRepository.revokeByAccountIds(List.of(id), Instant.now());
        }
        return modelMapper.map(accountRepository.saveAndFlush(account), AccountResponse.class);
    }

    @Override
    @Transactional
    public List<AccountResponse> updateStatusList(List<Long> ids, EN_Status status, String subject) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ids);
        List<Account> accounts = accountRepository.findAllById(uniqueIds);
        if (accounts.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều tài khoản không tồn tại");
        }
        for (Account account : accounts) {
            // DELETED là soft delete; ACTIVE/INACTIVE cũng áp dụng cho bản ghi đã xóa.
            account.setStatus(status);
            account.setTokenVersion(account.getTokenVersion() + 1);
            account.setUpdatedBy(Long.valueOf(subject));
        }
        List<Account> updated = accountRepository.saveAllAndFlush(accounts);
        refreshTokenRepository.revokeByAccountIds(uniqueIds, Instant.now());
        return updated.stream().map(account -> modelMapper.map(account, AccountResponse.class)).toList();
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
